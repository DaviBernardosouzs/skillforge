package dev.skillforge.template;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.parsing.FrontmatterSplitter;
import dev.skillforge.parsing.ParseResult;
import dev.skillforge.parsing.SkillParser;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SkillMarkdownWriterTest {

    private final SkillMarkdownWriter writer = new SkillMarkdownWriter();
    private final SkillParser parser = new SkillParser(new FrontmatterSplitter());

    @Test
    void writesCanonicalFrontmatterInFixedOrder() {
        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("license", "MIT");
        extras.put("allowed-tools", "Bash");

        String md = writer.write("minha-skill", "Faz algo. Use quando precisar.", extras, "\n\n# Corpo\n\n");

        assertThat(md).isEqualTo("""
                ---
                name: minha-skill
                description: Faz algo. Use quando precisar.
                license: MIT
                allowed-tools: Bash
                ---

                # Corpo
                """);
    }

    @Test
    void roundTripsValuesThatNeedQuoting() {
        String description = "Revisa: PRs \"Java\" #1. Use quando o usuário pedir: revisão. " + "x".repeat(200);

        String md = writer.write("a", description, Map.of("version", "1.0"), "corpo");
        RawSkill parsed = ((ParseResult.Success) parser.parse(md)).skill();

        assertThat(parsed.description()).isEqualTo(description);
        assertThat(parsed.extraFrontmatter()).containsEntry("version", "1.0");
        assertThat(md.lines().filter(l -> l.startsWith("description:"))).hasSize(1);
    }
}
