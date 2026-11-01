package dev.skillforge.authoring.rules;

import dev.skillforge.authoring.DraftAnalysis;
import dev.skillforge.authoring.QualityHint;
import dev.skillforge.authoring.QualityRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Poucas situações de uso deixam a skill difícil de ser acionada. */
@Component
public class FewTriggersRule implements QualityRule {

    public static final String ID = "few-triggers";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Optional<QualityHint> check(DraftAnalysis analysis) {
        if (analysis.draft().triggers().size() < 2) {
            return Optional.of(new QualityHint(ID, "Liste pelo menos 2 situações de uso: quanto mais variadas, mais fácil o agente reconhecer quando acionar a skill."));
        }
        return Optional.empty();
    }
}
