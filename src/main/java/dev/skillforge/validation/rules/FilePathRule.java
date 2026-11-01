package dev.skillforge.validation.rules;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.SkillFile;
import dev.skillforge.domain.Violation;
import dev.skillforge.validation.LintContext;
import dev.skillforge.validation.LintRule;
import dev.skillforge.validation.SkillFilePathValidator;
import dev.skillforge.validation.SkillLabels;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Bloqueia path traversal: arquivos só dentro de scripts/, references/ ou assets/ da skill. */
@Component
public class FilePathRule implements LintRule {

    public static final String ID = "file-path";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<Violation> check(RawSkill skill, LintContext ctx) {
        List<Violation> violations = new ArrayList<>();
        for (SkillFile file : skill.files()) {
            SkillFilePathValidator.problemWith(file.relativePath()).ifPresent(problem ->
                    violations.add(Violation.error(ID, SkillLabels.of(skill),
                            "Arquivo '" + file.relativePath() + "' recusado: " + problem + ".")));
        }
        return violations;
    }
}
