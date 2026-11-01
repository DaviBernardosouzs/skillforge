package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Violation;
import dev.skillforge.validation.LintContext;
import dev.skillforge.validation.LintRule;
import dev.skillforge.validation.SkillLabels;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Cada skill em {@code related} precisa existir na biblioteca (senão a ligação some do grafo). */
@Component
public class RelatedExistsRule implements LintRule {

    public static final String ID = "related-exists";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<Violation> check(RawSkill skill, LintContext ctx) {
        Set<String> names = ctx.allSkills().stream()
                .map(RawSkill::name)
                .filter(Objects::nonNull)
                .map(String::strip)
                .collect(Collectors.toSet());
        return skill.graph().related().stream()
                .filter(related -> !names.contains(related))
                .map(related -> Violation.warning(ID, SkillLabels.of(skill),
                        "A skill relacionada '" + related + "' não existe nesta biblioteca."))
                .toList();
    }
}
