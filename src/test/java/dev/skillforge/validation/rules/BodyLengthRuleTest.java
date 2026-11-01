package dev.skillforge.validation.rules;

import dev.skillforge.domain.Severity;
import org.junit.jupiter.api.Test;

import static dev.skillforge.validation.RawSkills.ctx;
import static dev.skillforge.validation.RawSkills.withBody;
import static org.assertj.core.api.Assertions.assertThat;

class BodyLengthRuleTest {

    private final BodyLengthRule rule = new BodyLengthRule();

    @Test
    void passesAtLimit() {
        var skill = withBody("linha\n".repeat(500));
        assertThat(rule.check(skill, ctx(skill))).isEmpty();
    }

    @Test
    void warnsAboveLimitSuggestingReferences() {
        var skill = withBody("linha\n".repeat(501));
        assertThat(rule.check(skill, ctx(skill))).singleElement().satisfies(v -> {
            assertThat(v.severity()).isEqualTo(Severity.WARNING);
            assertThat(v.message()).contains("references/");
        });
    }
}
