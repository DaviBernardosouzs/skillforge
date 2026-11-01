package dev.skillforge.domain;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Arquivo de apoio de uma skill, com caminho relativo à pasta dela (ex.: references/checklist.md).
 * equals/hashCode/toString são sobrescritos porque o record padrão compararia o array por referência.
 */
public record SkillFile(String relativePath, byte[] content) {

    public SkillFile {
        Objects.requireNonNull(relativePath, "relativePath");
        content = content == null ? new byte[0] : content.clone();
    }

    public static SkillFile ofText(String relativePath, String text) {
        return new SkillFile(relativePath, text.getBytes(StandardCharsets.UTF_8));
    }

    /** Devolve uma cópia para manter o record imutável. */
    @Override
    public byte[] content() {
        return content.clone();
    }

    public int size() {
        return content.length;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SkillFile that
                && relativePath.equals(that.relativePath)
                && Arrays.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return 31 * relativePath.hashCode() + Arrays.hashCode(content);
    }

    @Override
    public String toString() {
        return "SkillFile[relativePath=" + relativePath + ", bytes=" + content.length + "]";
    }
}
