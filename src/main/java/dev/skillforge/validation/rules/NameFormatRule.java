package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.SkillName;
import dev.skillforge.domain.Violation;
import dev.skillforge.validation.LintContext;
import dev.skillforge.validation.LintRule;
import dev.skillforge.validation.SkillLabels;
import org.springframework.stereotype.Component;

import java.util.List;

/** {@code name} em kebab-case e dentro do limite de tamanho. */
@Component
public class NameFormatRule implements LintRule {

    public static final String ID = "name-format";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<Violation> check(RawSkill skill, LintContext ctx) {
        String name = skill.name();
        if (SkillLabels.isBlank(name)) {
            return List.of(); // coberto por name-required
        }
        int max = ctx.limits().nameMaxLength();
        if (!SkillName.isKebabCase(name)) {
            return List.of(Violation.error(ID, SkillLabels.of(skill),
                    "O name '" + name + "' precisa estar em kebab-case: letras minúsculas, números e hífens simples (ex.: revisao-de-codigo)."));
        }
        if (name.length() > max) {
            return List.of(Violation.error(ID, SkillLabels.of(skill),
                    "O name tem " + name.length() + " caracteres; o máximo é " + max + "."));
        }
        return List.of();
    }
}
