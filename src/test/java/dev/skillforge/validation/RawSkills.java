package dev.skillforge.validation;

import dev.skillforge.config.SkillLimits;
import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.SkillFile;

import java.util.List;
import java.util.Map;

/** Fábrica de skills para testes de regras. */
public final class RawSkills {

    public static final String VALID_DESCRIPTION = "Revisa PRs Java. Use quando o usuário pedir revisão de código.";

    private RawSkills() {
    }

    public static RawSkill valid(String name) {
        return new RawSkill(name, VALID_DESCRIPTION, "# Instruções\n\nFaça a revisão.", Map.of(), List.of());
    }

    public static RawSkill withName(String name) {
        return valid(name);
    }

    public static RawSkill withDescription(String description) {
        return new RawSkill("skill", description, "corpo", Map.of(), List.of());
    }

    public static RawSkill withBody(String body, SkillFile... files) {
        return new RawSkill("skill", VALID_DESCRIPTION, body, Map.of(), List.of(files));
    }

    public static LintContext ctx(RawSkill... all) {
        return new LintContext(List.of(all), SkillLimits.defaults());
    }
}
