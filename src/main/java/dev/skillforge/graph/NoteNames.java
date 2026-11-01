package dev.skillforge.graph;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Nomes de nota válidos no Obsidian e no Windows. Os acentos saem do nome do arquivo (o texto
 * exibido continua com acento): alguns unzip ignoram a marcação UTF-8 do zip e corrompem nomes
 * não ASCII, o que deixaria o git de quem descompacta com o working tree sujo.
 */
public final class NoteNames {

    /** \ / : * ? " < > | e também # ^ [ ], que quebram wikilinks. */
    private static final Pattern INVALID = Pattern.compile("[\\\\/:*?\"<>|#^\\[\\]]");

    private NoteNames() {
    }

    public static String sanitize(String name) {
        String ascii = Normalizer.normalize(name == null ? "" : name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\x20-\\x7E]", "");
        String cleaned = INVALID.matcher(ascii).replaceAll("")
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
