package dev.skillforge.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Utilitários internos do domínio. */
final class CollectionsUtil {

    private CollectionsUtil() {
    }

    /** Cópia imutável que preserva a ordem de inserção (Map.copyOf não preserva). */
    static <K, V> Map<K, V> orderedCopy(Map<K, V> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
