package dev.skillforge.validation;

import dev.skillforge.config.SkillLimits;
import dev.skillforge.domain.RawSkill;

import java.util.List;

/** O que uma regra pode consultar além da própria skill: as outras skills da biblioteca e os limites. */
public record LintContext(List<RawSkill> allSkills, SkillLimits limits) {

    public LintContext {
        allSkills = List.copyOf(allSkills);
    }
}
