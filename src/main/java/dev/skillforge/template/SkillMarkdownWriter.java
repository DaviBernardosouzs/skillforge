package dev.skillforge.template;

import org.springframework.stereotype.Component;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Escreve um SKILL.md de forma canônica: frontmatter com name, description e depois os campos
 * extras na ordem original, seguido do corpo. O YAML é gerado pelo Jackson (e não por template de
 * texto) para que valores com ":" ou aspas continuem válidos.
 */
@Component
public class SkillMarkdownWriter {

    private final YAMLMapper yaml = YAMLMapper.builder()
            .disable(YAMLWriteFeature.WRITE_DOC_START_MARKER)
            .disable(YAMLWriteFeature.SPLIT_LINES)
            .enable(YAMLWriteFeature.MINIMIZE_QUOTES)
            .enable(YAMLWriteFeature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
            .build();

    public String write(String name, String description, Map<String, Object> extras, String body) {
        Map<String, Object> frontmatter = new LinkedHashMap<>();
        frontmatter.put("name", name);
        frontmatter.put("description", description);
        frontmatter.putAll(extras);

        String trimmedBody = body == null ? "" : body.strip();
        StringBuilder out = new StringBuilder("---\n")
                .append(yaml.writeValueAsString(frontmatter))
                .append("---\n");
        if (!trimmedBody.isEmpty()) {
            out.append('\n').append(trimmedBody).append('\n');
        }
        return out.toString();
    }
}
