package dev.skillforge.domain;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Normalização de caminhos relativos no estilo POSIX ("/"), sem depender do sistema de arquivos.
 * Ex.: "./references/../references/a.md" vira "references/a.md".
 */
public final class RelativePaths {

    private RelativePaths() {
    }

    /**
     * Remove segmentos "." e vazios e resolve "..". Um ".." que sairia da raiz é mantido
     * (ex.: "../x" continua "../x"), para que quem valida consiga detectar a fuga.
     */
    public static String normalize(String path) {
        String unified = path.replace('\\', '/');
        Deque<String> segments = new ArrayDeque<>();
        for (String segment : unified.split("/")) {
            if (segment.isEmpty() || segment.equals(".")) {
                continue;
            }
            if (segment.equals("..") && !segments.isEmpty() && !segments.peekLast().equals("..")) {
                segments.removeLast();
            } else {
                segments.addLast(segment);
            }
        }
        String joined = String.join("/", segments);
        return unified.startsWith("/") ? "/" + joined : joined;
    }
}
