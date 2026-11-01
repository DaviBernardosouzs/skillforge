package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Violation;
import dev.skillforge.validation.LintContext;
import dev.skillforge.validation.LintRule;
import dev.skillforge.validation.SkillLabels;
import org.springframework.stereotype.Component;

import java.util.List;

/** Corpo longo demais gasta contexto; detalhes devem ir para references/. */
@Component
public class BodyLengthRule implements LintRule {

    public static final String ID = "body-length";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<Violation> check(RawSkill skill, LintContext ctx) {
        long lines = skill.body().lines().count();
        int max = ctx.limits().bodyMaxLines();
        if (lines > max) {
            return List.of(Violation.warning(ID, SkillLabels.of(skill),
                    "O corpo tem " + lines + " linhas (recomendado: até " + max
                            + "). Mova detalhes para arquivos em references/ e linke-os no SKILL.md."));
        }
        return List.of();
    }
}
