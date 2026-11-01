package dev.skillforge.domain;

import java.util.List;

/**
 * Nó do grafo.
 *
 * @param id          identificador estável (ex.: "skill:revisao", "category:dev")
 * @param label       texto exibido
 * @param path        arquivo real no repositório gerado (SKILL.md, referência, script) ou null
 * @param description description da skill (só em nós SKILL)
 * @param category    categoria da skill (só em nós SKILL)
 * @param tags        tags definidas pelo usuário (só em nós SKILL)
 */
public record GraphNode(String id, Type type, String label, String path, String description,
                        String category, List<String> tags) {

    public enum Type { INDEX, CATEGORY, SKILL, REFERENCE, SCRIPT }

    public GraphNode {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
