package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Severity;
import dev.skillforge.domain.SkillGraphInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.skillforge.validation.RawSkills.ctx;
import static dev.skillforge.validation.RawSkills.valid;
import static org.assertj.core.api.Assertions.assertThat;

class RelatedExistsRuleTest {

    private final RelatedExistsRule rule = new RelatedExistsRule();

    @Test
    void passesWhenRelatedSkillExists() {
        RawSkill a = valid("a").withGraph(new SkillGraphInfo(null, List.of(), List.of("b")));
        RawSkill b = valid("b");

        assertThat(rule.check(a, ctx(a, b))).isEmpty();
    }

    @Test
    void warnsForMissingRelatedSkill() {
        RawSkill a = valid("a").withGraph(new SkillGraphInfo(null, List.of(), List.of("fantasma")));

        assertThat(rule.check(a, ctx(a))).singleElement().satisfies(v -> {
            assertThat(v.severity()).isEqualTo(Severity.WARNING);
            assertThat(v.message()).contains("fantasma");
        });
    }
}
