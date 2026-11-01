package dev.skillforge.generation;

import dev.skillforge.domain.Library;
import dev.skillforge.domain.LibraryGraph;
import dev.skillforge.domain.Skill;
import dev.skillforge.domain.SkillFactory;
import dev.skillforge.graph.LibraryGraphBuilder;
import dev.skillforge.graph.ObsidianVaultWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Orquestra a geração: parse → lint (para com 422 se houver erro) → conversão para Skill →
 * escrita do repositório → vault Obsidian → git → zip. A pasta temporária é apagada sempre.
 */
@Service
public class LibraryGenerator {

    private static final Logger log = LoggerFactory.getLogger(LibraryGenerator.class);
    static final String TEMP_PREFIX = "skillforge-";

    private final LibraryAnalyzer analyzer;
    private final RepositoryWriter repositoryWriter;
    private final LibraryGraphBuilder graphBuilder;
    private final ObsidianVaultWriter vaultWriter;
    private final GitInitializer gitInitializer;
    private final ZipPackager zipPackager;

    public LibraryGenerator(LibraryAnalyzer analyzer, RepositoryWriter repositoryWriter,
                            LibraryGraphBuilder graphBuilder, ObsidianVaultWriter vaultWriter,
                            GitInitializer gitInitializer, ZipPackager zipPackager) {
        this.analyzer = analyzer;
        this.repositoryWriter = repositoryWriter;
        this.graphBuilder = graphBuilder;
        this.vaultWriter = vaultWriter;
        this.gitInitializer = gitInitializer;
        this.zipPackager = zipPackager;
    }

    /**
     * @throws LintFailedException se alguma skill tiver violação ERROR
     * @throws dev.skillforge.validation.LimitExceededException se passar dos limites de tamanho
     */
    public GeneratedLibrary generate(LibraryInput input) {
        LibraryAnalyzer.Analysis analysis = analyzer.analyze(input.skills());
        if (analysis.report().hasErrors()) {
            throw new LintFailedException(analysis.report());
        }

        List<Skill> skills = analysis.parsed().stream().map(SkillFactory::fromRaw).toList();
        Library library = new Library(input.name(), input.author(), input.license(), skills);

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory(TEMP_PREFIX);
            Path root = tempDir.resolve(library.name());
            Files.createDirectories(root);

            repositoryWriter.write(library, root);
            LibraryGraph graph = graphBuilder.build(library);
            vaultWriter.write(graph, root);
            gitInitializer.init(root, library.author());

            ByteArrayOutputStream zip = new ByteArrayOutputStream();
            zipPackager.zip(root, zip);
            return new GeneratedLibrary(library.name() + ".zip", zip.toByteArray(), analysis.report());
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar a biblioteca", e);
        } finally {
            deleteQuietly(tempDir);
        }
    }

    static void deleteQuietly(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    log.warn("Não foi possível apagar {}", path, e);
                }
            });
        } catch (IOException e) {
            log.warn("Não foi possível limpar a pasta temporária {}", dir, e);
        }
    }
}
