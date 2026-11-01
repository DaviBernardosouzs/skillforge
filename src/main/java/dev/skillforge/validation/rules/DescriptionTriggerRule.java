package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Violation;
import dev.skillforge.validation.LintContext;
import dev.skillforge.validation.LintRule;
import dev.skillforge.validation.SkillLabels;
import java.util.Locale;
import org.springframework.stereotype.Component;

import java.util.List;

/** A description deveria dizer quando a skill deve ser acionada. */
@Component
public class DescriptionTriggerRule implements LintRule {

    public static final String ID = "description-trigger";

    static final List<String> TRIGGER_PHRASES = List.of("use quando", "use when", "quando o usuário", "when the user");

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<Violation> check(RawSkill skill, LintContext ctx) {
        if (SkillLabels.isBlank(skill.description())) {
            return List.of();
        }
        String description = skill.description().toLowerCase(Locale.ROOT);
        boolean saysWhen = TRIGGER_PHRASES.stream().anyMatch(description::contains);
        if (!saysWhen) {
            return List.of(Violation.warning(ID, SkillLabels.of(skill),
                    "A description não diz quando usar a skill. Inclua algo como \"Use quando o usuário pedir...\"."));
        }
        return List.of();
    }
}
