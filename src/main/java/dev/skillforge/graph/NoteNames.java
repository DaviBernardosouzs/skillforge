package dev.skillforge.graph;

import java.util.regex.Pattern;

/** Nomes de nota válidos no Obsidian e no Windows. */
public final class NoteNames {

    /** \ / : * ? " < > | e também # ^ [ ], que quebram wikilinks. */
    private static final Pattern INVALID = Pattern.compile("[\\\\/:*?\"<>|#^\\[\\]]");

    private NoteNames() {
    }

    public static String sanitize(String name) {
        String cleaned = INVALID.matcher(name == null ? "" : name).replaceAll("")
                .replaceAll("\\p{Cntrl}", "")
                .replaceAll("\\s+", " ")
                .strip();
        // Windows não aceita nome terminado em ponto ou espaço
        cleaned = cleaned.replaceAll("[. ]+$", "");
        return cleaned.isEmpty() ? "sem-nome" : cleaned;
    }

    /**
     * Tag válida no Obsidian: letras, números, _, - e /, sem começar com número.
     */
    public static String tag(String raw) {
        String tag = raw.strip().replaceFirst("^#", "").replaceAll("\\s+", "-")
                .replaceAll("[^\\p{L}\\p{N}_/-]", "");
        if (tag.isEmpty()) {
            return "";
        }
        return Character.isDigit(tag.charAt(0)) ? "_" + tag : tag;
    }
}
