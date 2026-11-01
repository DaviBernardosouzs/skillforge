package dev.skillforge.authoring.rules;

import dev.skillforge.authoring.DraftAnalysis;
import dev.skillforge.authoring.QualityHint;
import dev.skillforge.authoring.QualityRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Exemplos são a forma mais eficiente de mostrar o resultado esperado. */
@Component
public class NoExamplesRule implements QualityRule {

    public static final String ID = "no-examples";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Optional<QualityHint> check(DraftAnalysis analysis) {
        if (analysis.draft().examples().isEmpty()) {
            return Optional.of(new QualityHint(ID, "Adicione pelo menos um exemplo de entrada e saída esperada: exemplos concretos ensinam mais que regras."));
        }
        return Optional.empty();
    }
}
