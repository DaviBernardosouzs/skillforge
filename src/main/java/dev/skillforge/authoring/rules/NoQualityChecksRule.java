package dev.skillforge.authoring.rules;

import dev.skillforge.authoring.DraftAnalysis;
import dev.skillforge.authoring.QualityHint;
import dev.skillforge.authoring.QualityRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Um checklist final pega erros antes de chegarem ao usuário. */
@Component
public class NoQualityChecksRule implements QualityRule {

    public static final String ID = "no-quality-checks";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Optional<QualityHint> check(DraftAnalysis analysis) {
        if (analysis.draft().qualityChecks().isEmpty()) {
            return Optional.of(new QualityHint(ID, "Inclua uma verificação antes de entregar: itens objetivos que o agente confere antes de responder."));
        }
        return Optional.empty();
    }
}
