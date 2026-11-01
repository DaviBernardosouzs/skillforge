package dev.skillforge.authoring.rules;

import dev.skillforge.authoring.DraftAnalysis;
import dev.skillforge.authoring.QualityHint;
import dev.skillforge.authoring.QualityRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Passos com detalhe curto demais costumam ser vagos. */
@Component
public class VagueStepsRule implements QualityRule {

    public static final String ID = "vague-steps";

    static final int MIN_WORDS = 5;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Optional<QualityHint> check(DraftAnalysis analysis) {
        if (analysis.draft().steps().stream().anyMatch(step -> wordCount(step.detail()) < MIN_WORDS)) {
            return Optional.of(new QualityHint(ID, "Algum passo do processo tem detalhe com menos de " + MIN_WORDS + " palavras. Explique o que fazer e por quê."));
        }
        return Optional.empty();
    }

    private static long wordCount(String text) {
        return text.isBlank() ? 0 : text.strip().split("\\s+").length;
    }
}
