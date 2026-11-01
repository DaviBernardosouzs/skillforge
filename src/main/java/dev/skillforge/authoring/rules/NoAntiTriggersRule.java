package dev.skillforge.authoring.rules;

import dev.skillforge.authoring.DraftAnalysis;
import dev.skillforge.authoring.QualityHint;
import dev.skillforge.authoring.QualityRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Sem "quando não usar", a skill corre risco de acionamento errado. */
@Component
public class NoAntiTriggersRule implements QualityRule {

    public static final String ID = "no-anti-triggers";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Optional<QualityHint> check(DraftAnalysis analysis) {
        if (analysis.draft().antiTriggers().isEmpty()) {
            return Optional.of(new QualityHint(ID, "Diga quando NÃO usar a skill. Isso evita que ela seja acionada em pedidos parecidos, mas diferentes."));
        }
        return Optional.empty();
    }
}
