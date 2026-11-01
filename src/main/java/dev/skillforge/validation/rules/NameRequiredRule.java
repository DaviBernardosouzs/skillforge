package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Violation;
import dev.skillforge.validation.LintContext;
import dev.skillforge.validation.LintRule;
import dev.skillforge.validation.SkillLabels;
import org.springframework.stereotype.Component;

import java.util.List;

/** O frontmatter precisa ter {@code name}. */
@Component
public class NameRequiredRule implements LintRule {

    public static final String ID = "name-required";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<Violation> check(RawSkill skill, LintContext ctx) {
        if (SkillLabels.isBlank(skill.name())) {
            return List.of(Violation.error(ID, SkillLabels.of(skill), "O campo 'name' é obrigatório no frontmatter."));
        }
        return List.of();
    }
}
