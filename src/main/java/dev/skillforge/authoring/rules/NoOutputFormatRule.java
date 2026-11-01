package dev.skillforge.authoring.rules;

import dev.skillforge.authoring.DraftAnalysis;
import dev.skillforge.authoring.QualityHint;
import dev.skillforge.authoring.QualityRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Sem formato de saída definido, o resultado varia a cada execução. */
@Component
public class NoOutputFormatRule implements QualityRule {

    public static final String ID = "no-output-format";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Optional<QualityHint> check(DraftAnalysis analysis) {
        if (analysis.draft().outputFormat().isBlank()) {
            return Optional.of(new QualityHint(ID, "Descreva o formato da saída: sem isso, cada execução pode entregar algo diferente."));
        }
        return Optional.empty();
    }
}
