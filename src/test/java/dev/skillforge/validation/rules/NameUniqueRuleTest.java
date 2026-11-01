package dev.skillforge.validation.rules;

import org.junit.jupiter.api.Test;

import static dev.skillforge.validation.RawSkills.ctx;
import static dev.skillforge.validation.RawSkills.valid;
import static org.assertj.core.api.Assertions.assertThat;

class NameUniqueRuleTest {

    private final NameUniqueRule rule = new NameUniqueRule();

    @Test
    void passesWhenNamesAreDistinct() {
        var a = valid("a");
        var b = valid("b");
        var ctx = ctx(a, b);

        assertThat(rule.check(a, ctx)).isEmpty();
        assertThat(rule.check(b, ctx)).isEmpty();
    }

    @Test
    void reportsOnlyTheRepeatedOccurrence() {
        var first = valid("a");
        var second = valid("a"); // igual por equals, diferente por referência
        var ctx = ctx(first, second);

        assertThat(rule.check(first, ctx)).isEmpty();
        assertThat(rule.check(second, ctx)).singleElement()
                .satisfies(v -> assertThat(v.ruleId()).isEqualTo("name-unique"));
    }
}
