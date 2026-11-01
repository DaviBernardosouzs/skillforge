package dev.skillforge.domain;

import java.util.List;
import java.util.Map;

/** Fábrica de skills válidas para testes de grafo e geração. */
public final class Skills {

    private Skills() {
    }

    public static Skill skill(String name, String body, SkillGraphInfo graph, SkillFile... files) {
        return new Skill(new SkillMetadata(new SkillName(name),
                "Faz " + name + ". Use quando o usuário pedir " + name + ".", Map.of()),
                body, List.of(files), graph);
    }

    public static Skill skill(String name) {
        return skill(name, "# " + name, SkillGraphInfo.empty());
    }

    public static SkillGraphInfo graph(String category, List<String> tags, List<String> related) {
        return new SkillGraphInfo(category, tags, related);
    }
}
