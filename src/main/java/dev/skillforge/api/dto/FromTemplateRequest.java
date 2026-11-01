package dev.skillforge.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Pedido para preencher um modelo pronto de SKILL.md. */
public record FromTemplateRequest(
        @Schema(description = "Id do modelo (veja GET /api/templates)", example = "code-review")
        @NotBlank String templateId,
        @Schema(description = "Nome da skill em kebab-case", example = "revisao-de-codigo")
        @NotBlank String name,
        @Schema(description = "Description da skill", example = "Revisa PRs Java. Use quando o usuário pedir revisão de código.")
        @NotBlank String description) {
}
