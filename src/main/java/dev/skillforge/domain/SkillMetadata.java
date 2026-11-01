package dev.skillforge.domain;

import java.util.Map;
import java.util.Objects;

/** Frontmatter de uma skill já validada. {@code extras} preserva a ordem original dos campos. */
public record SkillMetadata(SkillName name, String description, Map<String, Object> extras) {

    public SkillMetadata {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        extras = extras == null ? Map.of() : CollectionsUtil.orderedCopy(extras);
    }
}
