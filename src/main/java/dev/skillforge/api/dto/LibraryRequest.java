package dev.skillforge.api.dto;

import dev.skillforge.domain.License;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Descrição de uma biblioteca de skills a validar, visualizar ou gerar. */
@Schema(description = "Biblioteca de skills")
public record LibraryRequest(
        @Schema(description = "Nome em kebab-case; vira o nome da pasta e do zip", example = "minhas-skills")
        @NotBlank
        @Size(max = 64)
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "deve estar em kebab-case (ex.: minhas-skills)")
        String name,
        @Schema(description = "Autor, usado no README, na licença e no commit", example = "Bernardo")
        @NotBlank @Size(max = 100) String author,
        @Schema(description = "Licença do repositório gerado", example = "MIT")
        @NotNull License license,
        @Schema(description = "Skills da biblioteca")
        @NotEmpty List<@Valid @NotNull SkillRequest> skills) {
}
