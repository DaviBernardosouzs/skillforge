package dev.skillforge.parsing;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.SkillFile;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Lê o conteúdo de um SKILL.md e produz um {@link RawSkill} (ou uma falha descritiva). */
@Component
public class SkillParser {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final FrontmatterSplitter splitter;
    private final YAMLMapper yaml = new YAMLMapper();

    public SkillParser(FrontmatterSplitter splitter) {
        this.splitter = splitter;
    }

    public ParseResult parse(String content, List<SkillFile> files) {
        FrontmatterSplitter.Split split = splitter.split(content);
        Optional<String> frontmatter = split.frontmatter();
        if (frontmatter.isEmpty()) {
            return new ParseResult.Failure(
                    "O arquivo precisa começar com um bloco de frontmatter YAML entre linhas '---'.");
        }

        Map<String, Object> fields;
        try {
            fields = readYaml(frontmatter.get());
        } catch (JacksonException e) {
            return new ParseResult.Failure("O frontmatter não é um YAML válido no formato chave: valor. "
                    + firstLine(e.getOriginalMessage()));
        }

        String name = asText(fields.remove("name"));
        String description = asText(fields.remove("description"));
        return new ParseResult.Success(new RawSkill(name, description, split.body(), fields, files));
    }

    public ParseResult parse(String content) {
        return parse(content, List.of());
    }

    private Map<String, Object> readYaml(String text) {
        if (text.isBlank()) {
            return new LinkedHashMap<>();
        }
        LinkedHashMap<String, Object> map = yaml.readValue(text, MAP_TYPE);
        return map == null ? new LinkedHashMap<>() : map;
    }

    private static String asText(Object value) {
        return value == null ? null : value.toString();
    }

    private static String firstLine(String message) {
        if (message == null) {
            return "";
        }
        int newline = message.indexOf('\n');
        return newline < 0 ? message : message.substring(0, newline);
    }
}
