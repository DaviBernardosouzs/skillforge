package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Violation;
import dev.skillforge.validation.LintContext;
import dev.skillforge.validation.LintRule;
import dev.skillforge.validation.SkillLabels;
import org.springframework.stereotype.Component;

import java.util.List;

/** {@code description} dentro do limite de caracteres. */
@Component
public class DescriptionLengthRule implements LintRule {

    public static final String ID = "description-length";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<Violation> check(RawSkill skill, LintContext ctx) {
        if (SkillLabels.isBlank(skill.description())) {
            return List.of();
        }
        int length = skill.description().strip().length();
        int max = ctx.limits().descriptionMaxLength();
        if (length > max) {
            return List.of(Violation.error(ID, SkillLabels.of(skill),
                    "A description tem " + length + " caracteres; o máximo é " + max + "."));
        }
        return List.of();
    }
}
