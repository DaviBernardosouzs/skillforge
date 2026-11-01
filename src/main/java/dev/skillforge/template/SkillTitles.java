package dev.skillforge.template;

/** Deriva um título legível a partir do name em kebab-case: "revisao-de-codigo" → "Revisao de codigo". */
public final class SkillTitles {

    private SkillTitles() {
    }

    public static String fromName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String spaced = name.strip().replace('-', ' ');
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
