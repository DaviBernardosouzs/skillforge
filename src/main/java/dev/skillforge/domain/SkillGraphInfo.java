package dev.skillforge.domain;

import java.util.List;

/**
 * Metadados usados só no grafo do Obsidian. Não vão para o frontmatter do SKILL.md,
 * que continua no formato padrão de Agent Skills.
 */
public record SkillGraphInfo(String category, List<String> tags, List<String> related) {

    public SkillGraphInfo {
        category = category == null || category.isBlank() ? null : category.strip();
        tags = tags == null ? List.of() : tags.stream().filter(t -> t != null && !t.isBlank()).map(String::strip).toList();
        related = related == null ? List.of() : related.stream().filter(r -> r != null && !r.isBlank()).map(String::strip).toList();
    }

    public static SkillGraphInfo empty() {
        return new SkillGraphInfo(null, List.of(), List.of());
    }
}
