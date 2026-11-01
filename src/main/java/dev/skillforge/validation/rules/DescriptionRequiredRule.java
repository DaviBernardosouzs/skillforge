package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Violation;
import dev.skillforge.validation.LintContext;
import dev.skillforge.validation.LintRule;
import dev.skillforge.validation.SkillLabels;
import org.springframework.stereotype.Component;

import java.util.List;

/** O frontmatter precisa ter {@code description}. */
@Component
public class DescriptionRequiredRule implements LintRule {

    public static final String ID = "description-required";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<Violation> check(RawSkill skill, LintContext ctx) {
        if (SkillLabels.isBlank(skill.description())) {
            return List.of(Violation.error(ID, SkillLabels.of(skill),
                    "O campo 'description' é obrigatório: diga o que a skill faz e quando usá-la."));
        }
        return List.of();
    }
}
