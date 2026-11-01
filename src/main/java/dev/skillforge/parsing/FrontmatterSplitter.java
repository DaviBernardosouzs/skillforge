package dev.skillforge.parsing;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * Separa o bloco YAML do início do arquivo (entre a primeira linha "---" e a próxima "---") do corpo.
 * Aceita quebras de linha \n e \r\n.
 */
@Component
public class FrontmatterSplitter {

    private static final String DELIMITER = "---";

    /** {@code frontmatter} vazio quando o arquivo não começa com um bloco "---" fechado. */
    public record Split(Optional<String> frontmatter, String body) {
    }

    public Split split(String content) {
        String text = stripBom(content == null ? "" : content).replace("\r\n", "\n");
        String[] lines = text.split("\n", -1);

        if (lines.length == 0 || !isDelimiter(lines[0])) {
            return new Split(Optional.empty(), text);
        }
        for (int i = 1; i < lines.length; i++) {
            if (isDelimiter(lines[i])) {
                String yaml = String.join("\n", Arrays.copyOfRange(lines, 1, i));
                String body = String.join("\n", Arrays.copyOfRange(lines, i + 1, lines.length));
                return new Split(Optional.of(yaml), body);
            }
        }
        // Abriu "---" mas nunca fechou: tratamos como frontmatter ausente
        return new Split(Optional.empty(), text);
    }

    private static boolean isDelimiter(String line) {
        return line.stripTrailing().equals(DELIMITER);
    }

    private static String stripBom(String text) {
        return text.startsWith("﻿") ? text.substring(1) : text;
    }
}
