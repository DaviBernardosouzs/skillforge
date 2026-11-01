package dev.skillforge.authoring;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Junta as dicas de todas as {@link QualityRule}. O linter diz o que está errado;
 * o advisor diz o que pode melhorar.
 */
@Component
public class DraftQualityAdvisor {

    private final List<QualityRule> rules;

    public DraftQualityAdvisor(List<QualityRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public List<QualityHint> advise(DraftAnalysis analysis) {
        return rules.stream()
                .map(rule -> rule.check(analysis))
                .flatMap(Optional::stream)
                .toList();
    }
}
