package dev.skillforge.authoring;

import dev.skillforge.authoring.SkillDraft.Step;
import dev.skillforge.authoring.rules.DescriptionTruncatedRule;
import dev.skillforge.config.SkillLimits;
import dev.skillforge.domain.SkillFile;
import dev.skillforge.parsing.FrontmatterSplitter;
import dev.skillforge.parsing.ParseResult;
import dev.skillforge.parsing.SkillParser;
import dev.skillforge.template.SkillMarkdownWriter;
import dev.skillforge.template.TemplateRenderer;
import dev.skillforge.validation.Linters;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkillDraftComposerTest {

    private final SkillDraftComposer composer = new SkillDraftComposer(new TemplateRenderer(),
            new SkillMarkdownWriter(), new SkillParser(new FrontmatterSplitter()), Linters.defaultLinter(),
            new DraftQualityAdvisor(QualityRules.all()), SkillLimits.defaults());

    @Test
    void minimalDraftHasNoEmptySectionsAndPassesLinter() {
        DraftResult result = composer.compose(Drafts.minimal());

        assertThat(result.skillMd()).isEqualTo("""
                ---
                name: revisao-de-codigo
                description: Revisa pull requests Java. Use quando o usuário pedir revisão de código.
                ---

                # Revisao de codigo

                Revisa pull requests Java.

                ## Quando usar
                - o usuário pedir revisão de código

                ## Processo
                1. **Leia o diff** — Leia todas as mudanças antes de comentar qualquer coisa.
                """);
        assertThat(result.stubFiles()).isEmpty();
        assertThat(result.lint().violations()).isEmpty();
    }

    @Test
    void completeDraftHasAllSectionsAndLinkedStubs() {
        DraftResult result = composer.compose(Drafts.complete());

        assertThat(result.skillMd()).contains(
                "## Quando usar", "## Quando NÃO usar", "## Entradas", "## Processo",
                "1. **Entenda a intenção**", "2. **Percorra o checklist**",
                "## Formato da saída", "## Exemplos", "### Exemplo 1", "**Entrada:** revisa esse PR",
                "## Armadilhas", "## Verificação antes de entregar", "- [ ] Todo comentário bloqueante explica o risco",
                "## Recursos",
                "- [checklist.md](references/checklist.md) — Critérios de revisão do time",
                "- [lint.sh](scripts/lint.sh) — Roda o linter no diff");
        assertThat(descriptionOf(result)).isEqualTo("Revisa pull requests Java seguindo o checklist do time. "
                + "Use quando o usuário pedir revisão de código, o usuário colar um diff.");
        assertThat(result.stubFiles()).extracting(SkillFile::relativePath)
                .containsExactly("references/checklist.md", "scripts/lint.sh");
        assertThat(text(result.stubFiles().get(0))).isEqualTo("# Checklist\n\nCritérios de revisão do time\n");
        assertThat(text(result.stubFiles().get(1))).startsWith("#!/usr/bin/env bash\n# Roda o linter no diff\n");
        assertThat(result.lint().violations()).isEmpty();
        assertThat(result.hints()).isEmpty();
    }

    @Test
    void longDescriptionIsTruncatedAndHinted() {
        SkillDraft draft = Drafts.with(d -> new SkillDraft(d.name(), "Faz algo útil. ".repeat(100), d.triggers(),
                d.antiTriggers(), d.inputs(), d.steps(), d.outputFormat(), d.examples(), d.pitfalls(),
                d.qualityChecks(), d.resources()));

        DraftResult result = composer.compose(draft);
        String description = descriptionOf(result);

        assertThat(description).hasSizeLessThanOrEqualTo(1024).endsWith("…");
        assertThat(result.hints()).extracting(QualityHint::id).containsExactly(DescriptionTruncatedRule.ID);
        assertThat(result.lint().errors()).isEmpty();
    }

    @Test
    void invalidNameIsReportedByLinterNotThrown() {
        SkillDraft draft = new SkillDraft("Nome Ruim", "Faz algo", List.of("x"), null, null,
                List.of(new Step("Passo", "")), null, null, null, null, null);

        assertThat(composer.compose(draft).lint().errors()).extracting(v -> v.ruleId()).containsExactly("name-format");
    }

    @Test
    void stepWithoutDetailHasNoDash() {
        SkillDraft draft = new SkillDraft("a", "Faz algo", List.of("x"), null, null,
                List.of(new Step("Só título", null)), null, null, null, null, null);

        assertThat(composer.compose(draft).skillMd()).contains("1. **Só título**\n").doesNotContain("**Só título** —");
    }

    @Test
    void truncateCutsAtWordBoundary() {
        assertThat(SkillDraftComposer.truncate("um dois tres quatro", 12)).isEqualTo("um dois…");
        assertThat(SkillDraftComposer.truncate("curto", 12)).isEqualTo("curto");
    }

    private static String descriptionOf(DraftResult result) {
        return ((ParseResult.Success) new SkillParser(new FrontmatterSplitter()).parse(result.skillMd()))
                .skill().description();
    }

    private static String text(SkillFile file) {
        return new String(file.content(), StandardCharsets.UTF_8);
    }
}
