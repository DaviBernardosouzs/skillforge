package dev.skillforge.validation.rules;

import dev.skillforge.domain.Severity;
import org.junit.jupiter.api.Test;

import static dev.skillforge.validation.RawSkills.ctx;
import static dev.skillforge.validation.RawSkills.withBody;
import static org.assertj.core.api.Assertions.assertThat;

class BodyNotEmptyRuleTest {

    private final BodyNotEmptyRule rule = new BodyNotEmptyRule();

    @Test
    void passesWithContent() {
        var skill = withBody("# Faça isso");
        assertThat(rule.check(skill, ctx(skill))).isEmpty();
    }

    @Test
    void warnsWhenBlank() {
        var skill = withBody(" \n\n ");
        assertThat(rule.check(skill, ctx(skill))).singleElement()
                .satisfies(v -> assertThat(v.severity()).isEqualTo(Severity.WARNING));
    }
}
