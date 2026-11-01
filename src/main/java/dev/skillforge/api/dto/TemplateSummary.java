package dev.skillforge.api.dto;

import dev.skillforge.template.SkillTemplate;

/** Modelo de skill sem o conteúdo, para listagem. */
public record TemplateSummary(String id, String title, String description) {

    public static TemplateSummary from(SkillTemplate template) {
        return new TemplateSummary(template.id(), template.title(), template.description());
    }
}
