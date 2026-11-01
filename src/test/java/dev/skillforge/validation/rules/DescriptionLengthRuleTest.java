package dev.skillforge.validation.rules;

import org.junit.jupiter.api.Test;

import static dev.skillforge.validation.RawSkills.ctx;
import static dev.skillforge.validation.RawSkills.withDescription;
import static org.assertj.core.api.Assertions.assertThat;

class DescriptionLengthRuleTest {

    private final DescriptionLengthRule rule = new DescriptionLengthRule();

    @Test
    void passesAtExactLimit() {
        var skill = withDescription("a".repeat(1024));
        assertThat(rule.check(skill, ctx(skill))).isEmpty();
    }

    @Test
    void failsAboveLimit() {
        var skill = withDescription("a".repeat(1025));
        assertThat(rule.check(skill, ctx(skill))).singleElement()
                .satisfies(v -> assertThat(v.message()).contains("1025").contains("1024"));
    }
}
