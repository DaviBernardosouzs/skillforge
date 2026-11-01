package dev.skillforge.authoring;

import java.util.Optional;

/** Regra de qualidade do assistente, no mesmo padrão Strategy do linter. */
public interface QualityRule {

    String id();

    Optional<QualityHint> check(DraftAnalysis analysis);
}
