package dev.skillforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Limites do formato de skill e do tamanho das requisições, lidos de {@code skillforge.limits}.
 */
@ConfigurationProperties(prefix = "skillforge.limits")
public record SkillLimits(
        int nameMaxLength,
        int descriptionMaxLength,
        int bodyMaxLines,
        int maxSkillsPerLibrary,
        long maxFileBytes,
        long maxLibraryBytes) {

    /** Os mesmos valores padrão do application.yml, úteis em testes sem Spring. */
    public static SkillLimits defaults() {
        return new SkillLimits(64, 1024, 500, 50, 1_048_576, 10_485_760);
    }
}
