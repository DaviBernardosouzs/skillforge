package dev.skillforge.validation;

import dev.skillforge.domain.RelativePaths;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Proteção contra path traversal / zip slip: o caminho de um arquivo enviado precisa ficar
 * dentro da pasta da skill e começar com uma das pastas permitidas.
 */
public final class SkillFilePathValidator {

    public static final List<String> ALLOWED_FOLDERS = List.of("scripts/", "references/", "assets/");

    private static final Pattern WINDOWS_DRIVE = Pattern.compile("^[a-zA-Z]:.*");

    private SkillFilePathValidator() {
    }

    /** Devolve o motivo da recusa, ou vazio se o caminho é seguro. */
    public static Optional<String> problemWith(String path) {
        if (path == null || path.isBlank()) {
            return Optional.of("caminho vazio");
        }
        if (path.indexOf('\0') >= 0) {
            return Optional.of("caminho contém caractere nulo");
        }
        String unified = path.replace('\\', '/');
        if (unified.startsWith("/") || WINDOWS_DRIVE.matcher(unified).matches()) {
            return Optional.of("caminho absoluto não é permitido");
        }
        String normalized = RelativePaths.normalize(unified);
        if (normalized.equals("..") || normalized.startsWith("../") || normalized.contains("/../")) {
            return Optional.of("o caminho sai da pasta da skill");
        }
        boolean allowed = ALLOWED_FOLDERS.stream()
                .anyMatch(folder -> normalized.startsWith(folder) && normalized.length() > folder.length());
        if (!allowed) {
            return Optional.of("o arquivo precisa estar em scripts/, references/ ou assets/");
        }
        return Optional.empty();
    }

    public static boolean isSafe(String path) {
        return problemWith(path).isEmpty();
    }
}
