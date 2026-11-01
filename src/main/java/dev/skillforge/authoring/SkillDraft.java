package dev.skillforge.authoring;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Respostas do formulário guiado. Só name, purpose, triggers e steps são obrigatórios;
 * o resto enriquece o SKILL.md gerado.
 */
@Schema(description = "Respostas do assistente de redação de skills")
public record SkillDraft(
        @Schema(description = "Nome em kebab-case", example = "revisao-de-codigo")
        @NotBlank String name,
        @Schema(description = "O que a skill faz, em uma frase", example = "Revisa pull requests Java seguindo o checklist do time.")
        @NotBlank String purpose,
        @Schema(description = "Situações em que a skill deve ser usada", example = "[\"o usuário pedir revisão de código\", \"o usuário colar um diff\"]")
        @NotEmpty List<@NotBlank String> triggers,
        @Schema(description = "Quando NÃO usar a skill")
        List<String> antiTriggers,
        @Schema(description = "O que a skill espera receber")
        List<String> inputs,
        @Schema(description = "Passo a passo do trabalho")
        @NotEmpty List<@Valid @NotNull Step> steps,
        @Schema(description = "Como deve ser a saída final")
        String outputFormat,
        @Schema(description = "Pares de entrada e saída esperada")
        List<@Valid @NotNull Example> examples,
        @Schema(description = "Erros comuns a evitar")
        List<String> pitfalls,
        @Schema(description = "Checklist antes de entregar")
        List<String> qualityChecks,
        @Schema(description = "Arquivos de apoio planejados")
        List<@Valid @NotNull ResourcePlan> resources) {

    public SkillDraft {
        triggers = clean(triggers);
        antiTriggers = clean(antiTriggers);
        inputs = clean(inputs);
        steps = steps == null ? List.of() : List.copyOf(steps);
        outputFormat = outputFormat == null ? "" : outputFormat.strip();
        examples = examples == null ? List.of() : List.copyOf(examples);
        pitfalls = clean(pitfalls);
        qualityChecks = clean(qualityChecks);
        resources = resources == null ? List.of() : List.copyOf(resources);
    }

    /** Remove itens nulos ou em branco das listas de texto. */
    private static List<String> clean(List<String> items) {
        return items == null ? List.of()
                : items.stream().filter(item -> item != null && !item.isBlank()).map(String::strip).toList();
    }

    public record Step(@NotBlank String title, String detail) {

        public Step {
            detail = detail == null ? "" : detail.strip();
        }
    }

    public record Example(@NotBlank String input, @NotBlank String expectedOutput) {
    }

    public record ResourcePlan(@NotNull ResourceKind kind, @NotBlank String fileName, String purpose) {

        public ResourcePlan {
            purpose = purpose == null ? "" : purpose.strip();
        }
    }

    public enum ResourceKind {
        SCRIPT("scripts"),
        REFERENCE("references"),
        ASSET("assets");

        private final String folder;

        ResourceKind(String folder) {
            this.folder = folder;
        }

        public String folder() {
            return folder;
        }
    }
}
