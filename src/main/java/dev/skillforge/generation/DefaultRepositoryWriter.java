package dev.skillforge.generation;

import dev.skillforge.config.SkillLimits;
import dev.skillforge.domain.Library;
import dev.skillforge.domain.License;
import dev.skillforge.domain.Skill;
import dev.skillforge.domain.SkillFile;
import dev.skillforge.template.SkillMarkdownWriter;
import dev.skillforge.template.TemplateRenderer;
import dev.skillforge.validation.SkillFilePathValidator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Clock;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Escreve README, LICENSE, .gitignore, as skills e os arquivos de apoio do repositório
 * (template de criação, guia, validate.sh e o workflow do GitHub Actions).
 */
@Component
public class DefaultRepositoryWriter implements RepositoryWriter {

    static final String VALIDATE_SCRIPT = "scripts/validate.sh";
    private static final Set<PosixFilePermission> EXECUTABLE = PosixFilePermissions.fromString("rwxr-xr-x");

    private final TemplateRenderer renderer;
    private final SkillMarkdownWriter markdownWriter;
    private final SkillLimits limits;
    private final Clock clock;

    public DefaultRepositoryWriter(TemplateRenderer renderer, SkillMarkdownWriter markdownWriter,
                                   SkillLimits limits, Clock clock) {
        this.renderer = renderer;
        this.markdownWriter = markdownWriter;
        this.limits = limits;
        this.clock = clock;
    }

    @Override
    public void write(Library library, Path root) throws IOException {
        Map<String, Object> ctx = context(library);

        writeText(root.resolve("README.md"), renderer.render("repo/README.md", ctx));
        if (library.license() != License.NONE) {
            writeText(root.resolve("LICENSE"), renderer.render("repo/LICENSE-" + library.license().name(), ctx));
        }
        writeText(root.resolve(".gitignore"), renderer.render("repo/gitignore", ctx));
        writeText(root.resolve("templates/SKILL.template.md"), renderer.render("repo/SKILL.template.md", ctx));
        writeText(root.resolve("docs/COMO-CRIAR-UMA-SKILL.md"), renderer.render("repo/COMO-CRIAR-UMA-SKILL.md", ctx));
        writeText(root.resolve(".github/workflows/validate.yml"), renderer.render("repo/validate.yml", ctx));

        Path script = root.resolve(VALIDATE_SCRIPT);
        writeText(script, renderer.render("repo/validate.sh", ctx));
        makeExecutable(script);

        for (Skill skill : library.skills()) {
            writeSkill(skill, root.resolve("skills").resolve(skill.name()));
        }
    }

    private void writeSkill(Skill skill, Path skillDir) throws IOException {
        String markdown = markdownWriter.write(skill.name(), skill.metadata().description(),
                skill.metadata().extras(), skill.body());
        writeText(skillDir.resolve("SKILL.md"), markdown);

        for (SkillFile file : skill.files()) {
            Path target = safeResolve(skillDir, file.relativePath());
            Files.createDirectories(target.getParent());
            Files.write(target, file.content());
            if (file.relativePath().startsWith("scripts/")) {
                makeExecutable(target);
            }
        }
    }

    /**
     * Segunda barreira contra path traversal: o linter já recusou caminhos ruins (regra file-path),
     * mas aqui conferimos de novo no sistema de arquivos antes de escrever.
     */
    static Path safeResolve(Path skillDir, String relativePath) {
        if (!SkillFilePathValidator.isSafe(relativePath)) {
            throw new IllegalArgumentException("Caminho de arquivo inseguro: " + relativePath);
        }
        Path target = skillDir.resolve(relativePath.replace('\\', '/')).normalize();
        if (!target.startsWith(skillDir.normalize())) {
            throw new IllegalArgumentException("Caminho de arquivo fora da pasta da skill: " + relativePath);
        }
        return target;
    }

    private Map<String, Object> context(Library library) {
        List<Map<String, String>> skills = library.skills().stream()
                .map(s -> Map.of("name", s.name(), "descriptionCell", tableCell(s.metadata().description())))
                .toList();
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("libraryName", library.name());
        ctx.put("author", library.author());
        ctx.put("year", Year.now(clock).getValue());
        ctx.put("skills", skills);
        ctx.put("hasLicense", library.license() != License.NONE);
        ctx.put("licenseLabel", library.license().displayName());
        ctx.put("nameMaxLength", limits.nameMaxLength());
        ctx.put("descriptionMaxLength", limits.descriptionMaxLength());
        ctx.put("bodyMaxLines", limits.bodyMaxLines());
        return ctx;
    }

    /** Texto seguro dentro de uma célula de tabela Markdown. */
    static String tableCell(String text) {
        return text.replace("\r", "").replace("\n", " ").replace("|", "\\|").strip();
    }

    private static void makeExecutable(Path file) throws IOException {
        PosixFileAttributeView posix = Files.getFileAttributeView(file, PosixFileAttributeView.class);
        if (posix != null) {
            posix.setPermissions(EXECUTABLE);
        }
    }

    private static void writeText(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }
}
