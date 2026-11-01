package dev.skillforge.parsing;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.SkillFile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkillParserTest {

    private final SkillParser parser = new SkillParser(new FrontmatterSplitter());

    @Test
    void parsesValidSkill() {
        String content = """
                ---
                name: revisao-de-codigo
                description: Revisa PRs. Use quando o usuário pedir revisão.
                ---

                # Revisão
                """;
        List<SkillFile> files = List.of(SkillFile.ofText("references/a.md", "x"));

        RawSkill skill = success(parser.parse(content, files));

        assertThat(skill.name()).isEqualTo("revisao-de-codigo");
        assertThat(skill.description()).isEqualTo("Revisa PRs. Use quando o usuário pedir revisão.");
        assertThat(skill.body()).isEqualTo("\n# Revisão\n");
        assertThat(skill.files()).isEqualTo(files);
        assertThat(skill.extraFrontmatter()).isEmpty();
    }

    @Test
    void preservesExtraFieldsInOriginalOrder() {
        String content = "---\nlicense: MIT\nname: a\nallowed-tools: Bash\ndescription: d\nmetadata:\n  version: 1\n---\n";

        RawSkill skill = success(parser.parse(content));

        assertThat(skill.extraFrontmatter().keySet()).containsExactly("license", "allowed-tools", "metadata");
        assertThat(skill.extraFrontmatter().get("metadata")).isEqualTo(java.util.Map.of("version", 1));
    }

    @Test
    void missingFrontmatterIsFailure() {
        assertThat(parser.parse("# Só corpo")).isInstanceOf(ParseResult.Failure.class);
    }

    @Test
    void brokenYamlIsFailureNotException() {
        ParseResult result = parser.parse("---\nname: [sem fechar\ndescription: x\n---\ncorpo");

        assertThat(result).isInstanceOfSatisfying(ParseResult.Failure.class,
                failure -> assertThat(failure.message()).contains("YAML"));
    }

    @Test
    void yamlThatIsNotAMapIsFailure() {
        assertThat(parser.parse("---\n- um\n- dois\n---\n")).isInstanceOf(ParseResult.Failure.class);
    }

    @Test
    void acceptsWindowsLineEndings() {
        RawSkill skill = success(parser.parse("---\r\nname: a\r\ndescription: b\r\n---\r\ncorpo\r\n"));

        assertThat(skill.name()).isEqualTo("a");
        assertThat(skill.body()).isEqualTo("corpo\n");
    }

    @Test
    void emptyFrontmatterAndBodyGiveNullFields() {
        RawSkill skill = success(parser.parse("---\n---\n"));

        assertThat(skill.name()).isNull();
        assertThat(skill.description()).isNull();
        assertThat(skill.body()).isEmpty();
    }

    private static RawSkill success(ParseResult result) {
        assertThat(result).isInstanceOf(ParseResult.Success.class);
        return ((ParseResult.Success) result).skill();
    }
}
