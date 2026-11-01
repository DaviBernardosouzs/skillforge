package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.RelativePaths;
import dev.skillforge.domain.Violation;
import dev.skillforge.parsing.ReferenceExtractor;
import dev.skillforge.validation.LintContext;
import dev.skillforge.validation.LintRule;
import dev.skillforge.validation.SkillLabels;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Todo link relativo do corpo precisa apontar para um arquivo enviado junto com a skill. */
@Component
public class ReferencesExistRule implements LintRule {

    public static final String ID = "references-exist";

    private final ReferenceExtractor extractor;

    public ReferencesExistRule(ReferenceExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<Violation> check(RawSkill skill, LintContext ctx) {
        Set<String> sent = skill.files().stream()
                .map(file -> RelativePaths.normalize(file.relativePath()))
                .collect(Collectors.toSet());
        return extractor.extract(skill.body()).stream()
                .filter(reference -> !sent.contains(reference))
                .map(reference -> Violation.error(ID, SkillLabels.of(skill),
                        "O corpo linka '" + reference + "', mas esse arquivo não foi enviado."))
                .toList();
    }
}
