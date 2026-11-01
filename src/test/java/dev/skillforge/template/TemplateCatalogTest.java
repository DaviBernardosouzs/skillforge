package dev.skillforge.template;

import dev.skillforge.domain.LintReport;
import dev.skillforge.parsing.FrontmatterSplitter;
import dev.skillforge.parsing.SkillParser;
import dev.skillforge.validation.Linters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateCatalogTest {

    private final FrontmatterSplitter splitter = new FrontmatterSplitter();
    private final TemplateCatalog catalog = new TemplateCatalog(splitter, new TemplateRenderer(), new SkillMarkdownWriter());

    @Test
    void loadsTheThreeBuiltInTemplates() {
        assertThat(catalog.all()).extracting(SkillTemplate::id)
                .containsExactly("code-review", "document-generator", "script-workflow");
        assertThat(catalog.all()).allSatisfy(t -> {
            assertThat(t.title()).isNotBlank();
            assertThat(t.description()).isNotBlank();
            assertThat(t.content()).contains("{{description}}").doesNotStartWith("---");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"code-review", "document-generator", "script-workflow"})
    void filledTemplatePassesLinterWithoutErrors(String id) {
        String md = catalog.fill(id, "minha-skill", "Faz algo. Use quando o usuário pedir.");

        LintReport report = Linters.defaultLinter().lintParsed(new SkillParser(splitter).parse(md));

        assertThat(md).startsWith("---\nname: minha-skill\n").contains("# Minha skill", "Faz algo.");
        assertThat(report.violations()).isEmpty();
    }

    @Test
    void unknownTemplateThrows() {
        assertThatThrownBy(() -> catalog.fill("nao-existe", "a", "b")).isInstanceOf(TemplateNotFoundException.class);
    }
}
