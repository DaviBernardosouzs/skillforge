package dev.skillforge.validation;

import dev.skillforge.config.SkillLimits;
import dev.skillforge.domain.LintReport;
import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.SkillFile;
import dev.skillforge.domain.Violation;
import dev.skillforge.parsing.FrontmatterSplitter;
import dev.skillforge.parsing.ReferenceExtractor;
import dev.skillforge.parsing.SkillParser;
import dev.skillforge.validation.rules.BodyLengthRule;
import dev.skillforge.validation.rules.BodyNotEmptyRule;
import dev.skillforge.validation.rules.DescriptionLengthRule;
import dev.skillforge.validation.rules.DescriptionRequiredRule;
import dev.skillforge.validation.rules.DescriptionTriggerRule;
import dev.skillforge.validation.rules.FilePathRule;
import dev.skillforge.validation.rules.NameFormatRule;
import dev.skillforge.validation.rules.NameRequiredRule;
import dev.skillforge.validation.rules.NameUniqueRule;
import dev.skillforge.validation.rules.ReferencesExistRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class SkillLinterTest {

    private final SkillParser parser = new SkillParser(new FrontmatterSplitter());

    private final SkillLinter linter = new SkillLinter(List.of(
            new NameRequiredRule(), new NameFormatRule(), new NameUniqueRule(),
            new DescriptionRequiredRule(), new DescriptionLengthRule(), new DescriptionTriggerRule(),
            new ReferencesExistRule(new ReferenceExtractor()), new BodyNotEmptyRule(), new BodyLengthRule(),
            new FilePathRule()), SkillLimits.defaults());

    @Test
    void validSkillHasNoViolations() {
        assertThat(linter.lint(RawSkills.valid("ok")).violations()).isEmpty();
    }

    @Test
    void aggregatesViolationsFromSeveralRulesAndSkills() {
        RawSkill badName = RawSkills.valid("Nome Ruim");
        RawSkill brokenLink = new RawSkill("links", RawSkills.VALID_DESCRIPTION, "[x](references/x.md)",
                Map.of(), List.of(SkillFile.ofText("../fora.md", "x")));
        RawSkill noTrigger = new RawSkill("sem-gatilho", "Faz algo.", "", Map.of(), List.of());

        LintReport report = linter.lint(List.of(badName, brokenLink, noTrigger));

        assertThat(report.violations()).extracting(Violation::ruleId, Violation::skill).containsExactlyInAnyOrder(
                tuple("name-format", "Nome Ruim"),
                tuple("references-exist", "links"),
                tuple("file-path", "links"),
                tuple("description-trigger", "sem-gatilho"),
                tuple("body-not-empty", "sem-gatilho"));
        assertThat(report.errors()).hasSize(3);
        assertThat(report.warnings()).hasSize(2);
    }

    @Test
    void parseFailureBecomesFrontmatterPresentViolation() {
        LintReport report = linter.lintParsed(List.of(
                parser.parse("# sem frontmatter"),
                parser.parse("---\nname: [quebrado\n---\n"),
                parser.parse("---\nname: ok\ndescription: " + RawSkills.VALID_DESCRIPTION + "\n---\n# corpo")));

        assertThat(report.violations()).extracting(Violation::ruleId, Violation::skill).containsExactly(
                tuple("frontmatter-present", "skill #1"),
                tuple("frontmatter-present", "skill #2"));
    }

    @Test
    void duplicatedNamesAcrossLibraryAreDetected() {
        LintReport report = linter.lint(List.of(RawSkills.valid("igual"), RawSkills.valid("igual")));

        assertThat(report.errors()).extracting(Violation::ruleId).containsExactly("name-unique");
    }
}
