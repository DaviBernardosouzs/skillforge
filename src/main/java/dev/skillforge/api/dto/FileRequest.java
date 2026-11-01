package dev.skillforge.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Arquivo de apoio de uma skill. Binários (ex.: imagens em assets/) vão em base64. */
@Schema(description = "Arquivo de apoio da skill")
public record FileRequest(
        @Schema(description = "Caminho relativo à pasta da skill, em scripts/, references/ ou assets/",
                example = "references/checklist.md")
        @NotBlank String path,
        @Schema(description = "Conteúdo do arquivo", example = "- Nomes claros\n- Testes")
        @NotNull String content,
        @Schema(description = "Codificação do conteúdo", allowableValues = {"utf8", "base64"}, defaultValue = "utf8")
        @Pattern(regexp = "utf8|base64", message = "deve ser utf8 ou base64") String encoding) {

    public boolean isBase64() {
        return "base64".equals(encoding);
    }
}
