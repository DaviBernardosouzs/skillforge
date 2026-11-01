package dev.skillforge.template;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateRendererTest {

    private final TemplateRenderer renderer = new TemplateRenderer();

    @Test
    void doesNotEscapeHtmlCharacters() {
        assertThat(renderer.renderString("{{v}}", Map.of("v", "<a> & \"b\""))).isEqualTo("<a> & \"b\"");
    }

    @Test
    void readmeListsSkillsAndOmitsLicenseSectionWhenAbsent() {
        String readme = renderer.render("repo/README.md", Map.of(
                "libraryName", "minhas-skills",
                "author", "Bernardo",
                "descriptionMaxLength", 1024,
                "skills", List.of(Map.of("name", "a", "descriptionCell", "Faz A."),
                        Map.of("name", "b", "descriptionCell", "Faz B.")),
                "hasLicense", false));

        assertThat(readme)
                .contains("# minhas-skills", "Gerada com [SkillForge]")
                .contains("| [a](skills/a/SKILL.md) | Faz A. |", "| [b](skills/b/SKILL.md) | Faz B. |")
                .contains("templates/SKILL.template.md", "./scripts/validate.sh")
                .doesNotContain("## Licença");
    }

    @Test
    void licenseTemplatesIncludeYearAndAuthor() {
        for (String license : List.of("MIT", "APACHE_2_0", "CC_BY_4_0")) {
            assertThat(renderer.render("repo/LICENSE-" + license, Map.of("year", 2026, "author", "Bernardo")))
                    .contains("2026 Bernardo");
        }
    }

    @Test
    void validateScriptReceivesConfiguredLimits() {
        String script = renderer.render("repo/validate.sh", Map.of("nameMaxLength", 64, "descriptionMaxLength", 1024));

        assertThat(script).startsWith("#!/usr/bin/env bash")
                .contains("set -euo pipefail", "NAME_MAX_LENGTH=64", "DESCRIPTION_MAX_LENGTH=1024");
    }

    @Test
    void workflowHasNoActionsExpressions() {
        String workflow = renderer.render("repo/validate.yml", Map.of());

        assertThat(workflow).contains("pull_request", "actions/checkout", "./scripts/validate.sh");
        assertThat(workflow.lines().filter(l -> !l.startsWith("#"))).noneMatch(l -> l.contains("${{"));
    }

    @Test
    void gitignoreKeepsObsidianConfigButIgnoresWorkspace() {
        assertThat(renderer.render("repo/gitignore", Map.of()))
                .contains(".DS_Store", "node_modules/", ".obsidian/workspace*.json")
                .doesNotContain("\n.obsidian/\n");
    }
}
