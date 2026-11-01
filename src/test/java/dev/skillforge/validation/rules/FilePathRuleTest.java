package dev.skillforge.validation.rules;

import dev.skillforge.domain.SkillFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.skillforge.validation.RawSkills.ctx;
import static dev.skillforge.validation.RawSkills.withBody;
import static org.assertj.core.api.Assertions.assertThat;

/** Testes de segurança: path traversal e pastas não permitidas. */
class FilePathRuleTest {

    private final FilePathRule rule = new FilePathRule();

    @ParameterizedTest
    @ValueSource(strings = {"references/a.md", "scripts/run.sh", "assets/img/logo.png", "./references/b.md",
            "references/x/../a.md"})
    void acceptsFilesInsideAllowedFolders(String path) {
        var skill = withBody("corpo", SkillFile.ofText(path, "x"));
        assertThat(rule.check(skill, ctx(skill))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"../x.md", "../../etc/passwd", "references/../../x", "/etc/passwd", "C:\\Windows\\x",
            "\\\\server\\share", "outra/a.md", "SKILL.md", "references/", "references/../SKILL.md", "", "..\\x"})
    void rejectsUnsafePaths(String path) {
        var skill = withBody("corpo", SkillFile.ofText(path, "x"));
        assertThat(rule.check(skill, ctx(skill))).singleElement()
                .satisfies(v -> assertThat(v.ruleId()).isEqualTo("file-path"));
    }
}
