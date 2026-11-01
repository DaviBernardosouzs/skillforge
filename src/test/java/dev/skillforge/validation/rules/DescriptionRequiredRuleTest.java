package dev.skillforge.validation.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.skillforge.validation.RawSkills.ctx;
import static dev.skillforge.validation.RawSkills.withDescription;
import static org.assertj.core.api.Assertions.assertThat;

class DescriptionRequiredRuleTest {

    private final DescriptionRequiredRule rule = new DescriptionRequiredRule();

    @Test
    void passesWhenPresent() {
        var skill = withDescription("Faz algo.");
        assertThat(rule.check(skill, ctx(skill))).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "  ")
    void failsWhenMissing(String description) {
        var skill = withDescription(description);
        assertThat(rule.check(skill, ctx(skill))).singleElement()
                .satisfies(v -> assertThat(v.ruleId()).isEqualTo("description-required"));
    }
}
