package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Violation;
import dev.skillforge.validation.LintContext;
import dev.skillforge.validation.LintRule;
import dev.skillforge.validation.SkillLabels;
import org.springframework.stereotype.Component;

import java.util.List;

/** O corpo deveria ter instruções. */
@Component
public class BodyNotEmptyRule implements LintRule {

    public static final String ID = "body-not-empty";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<Violation> check(RawSkill skill, LintContext ctx) {
        if (skill.body().isBlank()) {
            return List.of(Violation.warning(ID, SkillLabels.of(skill),
                    "O corpo do SKILL.md está vazio: escreva as instruções que a skill deve seguir."));
        }
        return List.of();
    }
}
