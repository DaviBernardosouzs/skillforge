package dev.skillforge.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Uma skill do LibraryRequest: o SKILL.md, os arquivos e os metadados opcionais do grafo. */
@Schema(description = "Skill da biblioteca")
public record SkillRequest(
        @Schema(description = "Conteúdo completo do SKILL.md (frontmatter + corpo)",
                example = "---\nname: revisao-de-codigo\ndescription: Revisa PRs Java. Use quando o usuário pedir revisão de código.\n---\n\n# Revisão de código\n\nSiga o [checklist](references/checklist.md).")
        @NotNull String content,
        @Schema(description = "Arquivos de apoio")
        List<@Valid @NotNull FileRequest> files,
        @Schema(description = "Categoria no grafo do Obsidian", example = "desenvolvimento")
        String category,
        @Schema(description = "Tags no grafo do Obsidian", example = "[\"java\", \"review\"]")
        List<String> tags,
        @Schema(description = "Nomes de outras skills da biblioteca relacionadas a esta", example = "[\"gerar-testes\"]")
        List<String> related) {
}
