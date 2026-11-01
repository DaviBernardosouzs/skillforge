package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Violation;
import dev.skillforge.validation.LintContext;
import dev.skillforge.validation.LintRule;
import dev.skillforge.validation.SkillLabels;
import org.springframework.stereotype.Component;

import java.util.List;

/** Duas skills da mesma biblioteca não podem ter o mesmo {@code name} (viram a mesma pasta). */
@Component
public class NameUniqueRule implements LintRule {

    public static final String ID = "name-unique";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<Violation> check(RawSkill skill, LintContext ctx) {
        if (SkillLabels.isBlank(skill.name())) {
            return List.of();
        }
        // Só a segunda ocorrência em diante é reportada, para não duplicar a mensagem
        for (RawSkill other : ctx.allSkills()) {
            if (other == skill) {
                return List.of();
            }
            if (skill.name().strip().equals(other.name() == null ? null : other.name().strip())) {
                return List.of(Violation.error(ID, SkillLabels.of(skill),
                        "Já existe outra skill chamada '" + skill.name().strip() + "' nesta biblioteca."));
            }
        }
        return List.of();
    }
}
