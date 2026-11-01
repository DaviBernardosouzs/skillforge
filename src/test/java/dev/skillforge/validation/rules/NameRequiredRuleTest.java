package dev.skillforge.validation.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.skillforge.validation.RawSkills.ctx;
import static dev.skillforge.validation.RawSkills.withName;
import static org.assertj.core.api.Assertions.assertThat;

class NameRequiredRuleTest {

    private final NameRequiredRule rule = new NameRequiredRule();

    @Test
    void passesWhenNamePresent() {
        var skill = withName("revisao");
        assertThat(rule.check(skill, ctx(skill))).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void failsWhenNameMissingOrBlank(String name) {
        var skill = withName(name);
        assertThat(rule.check(skill, ctx(skill)))
                .singleElement()
                .satisfies(v -> assertThat(v.ruleId()).isEqualTo("name-required"));
    }
}
