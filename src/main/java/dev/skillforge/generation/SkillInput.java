package dev.skillforge.generation;

import dev.skillforge.domain.SkillFile;
import dev.skillforge.domain.SkillGraphInfo;

import java.util.List;

/** Uma skill como chegou do cliente: o texto do SKILL.md, os arquivos já decodificados e os metadados do grafo. */
public record SkillInput(String content, List<SkillFile> files, SkillGraphInfo graph) {

    public SkillInput {
        content = content == null ? "" : content;
        files = files == null ? List.of() : List.copyOf(files);
        graph = graph == null ? SkillGraphInfo.empty() : graph;
    }
}
