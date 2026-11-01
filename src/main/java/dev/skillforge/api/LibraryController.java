package dev.skillforge.api;

import dev.skillforge.api.dto.LibraryRequest;
import dev.skillforge.api.dto.LintReportResponse;
import dev.skillforge.domain.LibraryGraph;
import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Skill;
import dev.skillforge.domain.SkillFactory;
import dev.skillforge.domain.SkillName;
import dev.skillforge.generation.GeneratedLibrary;
import dev.skillforge.generation.LibraryAnalyzer;
import dev.skillforge.generation.LibraryGenerator;
import dev.skillforge.generation.LibraryInput;
import dev.skillforge.graph.LibraryGraphBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/libraries")
@Tag(name = "Bibliotecas", description = "Validação, grafo e geração do repositório")
public class LibraryController {

    static final String LINT_WARNINGS_HEADER = "X-Lint-Warnings";
    static final MediaType APPLICATION_ZIP = MediaType.parseMediaType("application/zip");

    private final LibraryAnalyzer analyzer;
    private final LibraryGenerator generator;
    private final LibraryGraphBuilder graphBuilder;

    public LibraryController(LibraryAnalyzer analyzer, LibraryGenerator generator, LibraryGraphBuilder graphBuilder) {
        this.analyzer = analyzer;
        this.generator = generator;
        this.graphBuilder = graphBuilder;
    }

    @PostMapping("/lint")
    @Operation(summary = "Valida a biblioteca inteira",
            description = "Inclui regras entre skills, como name-unique e related-exists.")
    @ApiResponse(responseCode = "413", description = "Limites de quantidade ou tamanho estourados")
    public LintReportResponse lint(@Valid @RequestBody LibraryRequest request) {
        LibraryInput input = LibraryRequestMapper.toInput(request);
        return LintReportResponse.from(analyzer.analyze(input.skills()).report());
    }

    @PostMapping("/graph")
    @Operation(summary = "Pré-visualiza o grafo da biblioteca",
            description = "Nós e arestas do Graph View do Obsidian, sem gerar o zip. Skills com erro de nome ou "
                    + "description ficam de fora até serem corrigidas.")
    public LibraryGraph graph(@Valid @RequestBody LibraryRequest request) {
        LibraryInput input = LibraryRequestMapper.toInput(request);
        List<Skill> skills = convertible(analyzer.analyze(input.skills()).parsed());
        return graphBuilder.build(input.name(), skills);
    }

    @PostMapping(value = "/generate", produces = {"application/zip", MediaType.APPLICATION_PROBLEM_JSON_VALUE})
    @Operation(summary = "Gera o repositório Git da biblioteca em .zip",
            description = "Repositório com README, LICENSE, skills, validate.sh, GitHub Actions, vault Obsidian "
                    + "e um commit inicial. O header X-Lint-Warnings traz a quantidade de avisos.")
    @ApiResponse(responseCode = "200", description = "Zip do repositório",
            content = @Content(mediaType = "application/zip", schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "422", description = "Lint com erros; as violações vêm na propriedade violations")
    @ApiResponse(responseCode = "413", description = "Limites de quantidade ou tamanho estourados")
    public ResponseEntity<byte[]> generate(@Valid @RequestBody LibraryRequest request) {
        GeneratedLibrary library = generator.generate(LibraryRequestMapper.toInput(request));
        return ResponseEntity.ok()
                .contentType(APPLICATION_ZIP)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(library.fileName()).build().toString())
                .header(LINT_WARNINGS_HEADER, String.valueOf(library.report().warnings().size()))
                .body(library.zip());
    }

    /** Skills que já podem virar {@link Skill}: name válido e único, description presente. */
    private static List<Skill> convertible(List<RawSkill> parsed) {
        Set<String> seen = new HashSet<>();
        return parsed.stream()
                .filter(raw -> raw.name() != null && SkillName.isValid(raw.name().strip(), SkillName.MAX_LENGTH))
                .filter(raw -> raw.description() != null && !raw.description().isBlank())
                .filter(raw -> seen.add(raw.name().strip()))
                .map(raw -> SkillFactory.fromRaw(raw.withName(raw.name().strip())))
                .toList();
    }
}
