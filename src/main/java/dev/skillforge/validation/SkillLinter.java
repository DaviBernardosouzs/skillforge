package dev.skillforge.validation;

import dev.skillforge.config.SkillLimits;
import dev.skillforge.domain.LintReport;
import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Violation;
import dev.skillforge.parsing.ParseResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Roda todas as {@link LintRule} em todas as skills e junta as violações num único relatório.
 */
@Component
public class SkillLinter {

    /** Emitida quando o parser não conseguiu ler o frontmatter (ver {@link ParseResult.Failure}). */
    public static final String FRONTMATTER_PRESENT = "frontmatter-present";

    private final List<LintRule> rules;
    private final SkillLimits limits;

    public SkillLinter(List<LintRule> rules, SkillLimits limits) {
        this.rules = List.copyOf(rules);
        this.limits = limits;
    }

    public LintReport lint(RawSkill skill) {
        return lint(List.of(skill));
    }

    public LintReport lint(List<RawSkill> skills) {
        LintContext ctx = new LintContext(skills, limits);
        List<Violation> violations = new ArrayList<>();
        for (RawSkill skill : skills) {
            for (LintRule rule : rules) {
                violations.addAll(rule.check(skill, ctx));
            }
        }
        return new LintReport(violations);
    }

    public LintReport lintParsed(ParseResult result) {
        return lintParsed(List.of(result));
    }

    /**
     * Versão que aceita a saída do parser: falhas de leitura viram {@code frontmatter-present}
     * e as skills lidas com sucesso passam por todas as regras, juntas (para name-unique funcionar).
     */
    public LintReport lintParsed(List<ParseResult> results) {
        List<Violation> violations = new ArrayList<>();
        List<RawSkill> parsed = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            switch (results.get(i)) {
                case ParseResult.Success success -> parsed.add(success.skill());
                case ParseResult.Failure failure ->
                        violations.add(Violation.error(FRONTMATTER_PRESENT, "skill #" + (i + 1), failure.message()));
            }
        }
        violations.addAll(lint(parsed).violations());
        return new LintReport(violations);
    }
}
