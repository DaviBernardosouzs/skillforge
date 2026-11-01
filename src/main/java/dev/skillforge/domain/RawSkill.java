package dev.skillforge.domain;

import java.util.List;
import java.util.Map;

/**
 * Skill do jeito que veio do usuário: strings cruas, ainda sem validação.
 * O linter trabalha sobre este tipo para conseguir reportar todos os problemas de uma vez.
 */
public record RawSkill(String name, String description, String body,
                       Map<String, Object> extraFrontmatter, List<SkillFile> files, SkillGraphInfo graph) {

    public RawSkill(String name, String description, String body,
                    Map<String, Object> extraFrontmatter, List<SkillFile> files) {
        this(name, description, body, extraFrontmatter, files, SkillGraphInfo.empty());
    }

    public RawSkill {
        body = body == null ? "" : body;
        extraFrontmatter = extraFrontmatter == null ? Map.of() : CollectionsUtil.orderedCopy(extraFrontmatter);
        files = files == null ? List.of() : List.copyOf(files);
        graph = graph == null ? SkillGraphInfo.empty() : graph;
    }

    public RawSkill withGraph(SkillGraphInfo newGraph) {
        return new RawSkill(name, description, body, extraFrontmatter, files, newGraph);
    }
}
