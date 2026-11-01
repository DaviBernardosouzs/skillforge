package dev.skillforge.validation.rules;

import dev.skillforge.domain.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.skillforge.validation.RawSkills.ctx;
import static dev.skillforge.validation.RawSkills.withDescription;
import static org.assertj.core.api.Assertions.assertThat;

class DescriptionTriggerRuleTest {

    private final DescriptionTriggerRule rule = new DescriptionTriggerRule();

    @ParameterizedTest
    @ValueSource(strings = {
            "Revisa PRs. Use quando houver PR.",
            "Reviews PRs. USE WHEN asked.",
            "Gera docs quando o usuário pedir.",
            "Builds docs when the user asks."
    })
    void passesWhenDescriptionSaysWhenToUse(String description) {
        var skill = withDescription(description);
        assertThat(rule.check(skill, ctx(skill))).isEmpty();
    }

    @Test
    void warnsWhenTriggerIsMissing() {
        var skill = withDescription("Revisa PRs Java.");
        assertThat(rule.check(skill, ctx(skill))).singleElement()
                .satisfies(v -> assertThat(v.severity()).isEqualTo(Severity.WARNING));
    }
}
