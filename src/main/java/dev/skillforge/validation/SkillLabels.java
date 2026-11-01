package dev.skillforge.validation;

import dev.skillforge.domain.RawSkill;

/** Como uma skill aparece nas mensagens de violação. */
public final class SkillLabels {

    public static final String UNNAMED = "(sem nome)";

    private SkillLabels() {
    }

    public static String of(RawSkill skill) {
        return isBlank(skill.name()) ? UNNAMED : skill.name().strip();
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
