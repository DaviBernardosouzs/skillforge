package dev.skillforge.validation;

import dev.skillforge.config.SkillLimits;
import dev.skillforge.parsing.ReferenceExtractor;
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
import dev.skillforge.validation.rules.RelatedExistsRule;

import java.util.ArrayList;
import java.util.List;

/** Monta o linter com todas as regras, sem Spring, para testes unitários. */
public final class Linters {

    private Linters() {
    }

    public static List<LintRule> allRules() {
        return new ArrayList<>(List.of(
                new NameRequiredRule(), new NameFormatRule(), new NameUniqueRule(),
                new DescriptionRequiredRule(), new DescriptionLengthRule(), new DescriptionTriggerRule(),
                new ReferencesExistRule(new ReferenceExtractor()), new BodyNotEmptyRule(), new BodyLengthRule(),
                new FilePathRule(), new RelatedExistsRule()));
    }

    public static SkillLinter defaultLinter() {
        return new SkillLinter(allRules(), SkillLimits.defaults());
    }
}
