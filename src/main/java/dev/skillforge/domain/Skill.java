package dev.skillforge.domain;

import java.util.List;
import java.util.Objects;

/** Skill validada, pronta para ser escrita no repositório. */
public record Skill(SkillMetadata metadata, String body, List<SkillFile> files, SkillGraphInfo graph) {

    public Skill(SkillMetadata metadata, String body, List<SkillFile> files) {
        this(metadata, body, files, SkillGraphInfo.empty());
    }

    public Skill {
        Objects.requireNonNull(metadata, "metadata");
        body = body == null ? "" : body;
        files = files == null ? List.of() : List.copyOf(files);
        graph = graph == null ? SkillGraphInfo.empty() : graph;
    }

    public String name() {
        return metadata.name().value();
    }
}
