package dev.skillforge.template;

/** Modelo de SKILL.md oferecido ao usuário. {@code content} é o corpo, com placeholders Mustache. */
public record SkillTemplate(String id, String title, String description, String content) {
}
