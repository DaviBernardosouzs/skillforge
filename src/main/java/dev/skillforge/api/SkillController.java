package dev.skillforge.api;

import dev.skillforge.api.dto.DraftResponse;
import dev.skillforge.api.dto.LintReportResponse;
import dev.skillforge.api.dto.SkillLintRequest;
import dev.skillforge.authoring.DraftFieldGuide;
import dev.skillforge.authoring.SkillDraft;
import dev.skillforge.authoring.SkillDraftComposer;
import dev.skillforge.domain.SkillGraphInfo;
import dev.skillforge.generation.LibraryAnalyzer;
import dev.skillforge.generation.SkillInput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@Tag(name = "Skills", description = "Validação de uma skill e assistente de redação")
public class SkillController {

    private final LibraryAnalyzer analyzer;
    private final SkillDraftComposer composer;

    public SkillController(LibraryAnalyzer analyzer, SkillDraftComposer composer) {
        this.analyzer = analyzer;
        this.composer = composer;
    }

    @PostMapping("/lint")
    @Operation(summary = "Valida um SKILL.md",
            description = "Roda todas as regras do linter e devolve as violações (ERROR e WARNING).")
    public LintReportResponse lint(@Valid @RequestBody SkillLintRequest request) {
        SkillInput input = new SkillInput(request.content(), LibraryRequestMapper.toFiles(request.files()),
                SkillGraphInfo.empty());
        return LintReportResponse.from(analyzer.analyze(List.of(input)).report());
    }

    @GetMapping("/draft/template")
    @Operation(summary = "Campos do assistente de redação",
            description = "Nome, obrigatoriedade, pergunta-guia e exemplo de cada campo do SkillDraft, para montar o formulário.")
    public List<DraftFieldGuide.Field> draftTemplate() {
        return DraftFieldGuide.FIELDS;
    }

    @PostMapping("/draft")
    @Operation(summary = "Monta um SKILL.md a partir das respostas guiadas",
            description = "Devolve o SKILL.md, arquivos-esqueleto dos recursos, o resultado do linter e dicas de qualidade.")
    public DraftResponse draft(@Valid @RequestBody SkillDraft draft) {
        return DraftResponse.from(composer.compose(draft));
    }
}
