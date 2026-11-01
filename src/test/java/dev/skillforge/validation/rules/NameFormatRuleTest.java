package dev.skillforge.validation.rules;

import dev.skillforge.config.SkillLimits;
import dev.skillforge.validation.LintContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static dev.skillforge.validation.RawSkills.ctx;
import static dev.skillforge.validation.RawSkills.withName;
import static org.assertj.core.api.Assertions.assertThat;

class NameFormatRuleTest {

    private final NameFormatRule rule = new NameFormatRule();

    @ParameterizedTest
    @ValueSource(strings = {"revisao-de-codigo", "a1", "x"})
    void passesForKebabCase(String name) {
        var skill = withName(name);
        assertThat(rule.check(skill, ctx(skill))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Revisao", "revisao--codigo", "-revisao", "revisao-", "revisão", "revisao de codigo"})
    void failsForInvalidFormat(String name) {
        var skill = withName(name);
        assertThat(rule.check(skill, ctx(skill))).singleElement()
                .satisfies(v -> assertThat(v.message()).contains("kebab-case"));
    }

    @Test
    void respectsConfiguredMaxLength() {
        var skill = withName("abcdef");
        var limits = new SkillLimits(5, 1024, 500, 50, 1, 1);

        assertThat(rule.check(skill, new LintContext(List.of(skill), limits))).singleElement()
                .satisfies(v -> assertThat(v.message()).contains("máximo é 5"));
    }

    @Test
    void ignoresBlankNameBecauseNameRequiredCoversIt() {
        var skill = withName(" ");
        assertThat(rule.check(skill, ctx(skill))).isEmpty();
    }
}
