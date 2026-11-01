package dev.skillforge.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Um SKILL.md avulso para validar. */
public record SkillLintRequest(
        @Schema(description = "Conteúdo completo do SKILL.md",
                example = "---\nname: minha-skill\ndescription: Faz algo.\n---\n\n# Minha skill\n")
        @NotNull String content,
        @Schema(description = "Arquivos de apoio, para conferir os links do corpo")
        List<@Valid @NotNull FileRequest> files) {
}
