package dev.skillforge.domain;

import java.util.List;
import java.util.Map;

/**
 * Skill do jeito que veio do usuário: strings cruas, ainda sem validação.
 * O linter trabalha sobre este tipo para conseguir reportar todos os problemas de uma vez.
 */
public record RawSkill(String name, String description, String body,
                       Map<String, Object> extraFrontmatter, List<SkillFile> files) {

    public RawSkill {
        body = body == null ? "" : body;
        extraFrontmatter = extraFrontmatter == null ? Map.of() : CollectionsUtil.orderedCopy(extraFrontmatter);
        files = files == null ? List.of() : List.copyOf(files);
    }
}
