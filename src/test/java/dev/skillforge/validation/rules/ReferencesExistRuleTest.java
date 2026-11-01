package dev.skillforge.validation.rules;

import dev.skillforge.domain.SkillFile;
import dev.skillforge.parsing.ReferenceExtractor;
import org.junit.jupiter.api.Test;

import static dev.skillforge.validation.RawSkills.ctx;
import static dev.skillforge.validation.RawSkills.withBody;
import static org.assertj.core.api.Assertions.assertThat;

class ReferencesExistRuleTest {

    private final ReferencesExistRule rule = new ReferencesExistRule(new ReferenceExtractor());

    @Test
    void passesWhenEveryLinkHasAFile() {
        var skill = withBody("Veja o [checklist](./references/checklist.md) e [site](https://x.dev).",
                SkillFile.ofText("references/checklist.md", "- item"));
        assertThat(rule.check(skill, ctx(skill))).isEmpty();
    }

    @Test
    void reportsEachBrokenLink() {
        var skill = withBody("[a](references/a.md) [b](scripts/b.sh)", SkillFile.ofText("references/a.md", "a"));

        assertThat(rule.check(skill, ctx(skill))).singleElement()
                .satisfies(v -> assertThat(v.message()).contains("scripts/b.sh"));
    }
}
