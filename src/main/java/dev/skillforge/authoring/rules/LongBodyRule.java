package dev.skillforge.authoring.rules;

import dev.skillforge.authoring.DraftAnalysis;
import dev.skillforge.authoring.QualityHint;
import dev.skillforge.authoring.QualityRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Corpo longo gasta contexto do agente; detalhes devem ir para references/. */
@Component
public class LongBodyRule implements QualityRule {

    public static final String ID = "long-body";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Optional<QualityHint> check(DraftAnalysis analysis) {
        if (analysis.bodyLines() > analysis.limits().bodyMaxLines()) {
            return Optional.of(new QualityHint(ID, "O SKILL.md tem " + analysis.bodyLines() + " linhas (recomendado: até " + analysis.limits().bodyMaxLines() + "). Mova detalhes para arquivos em references/."));
        }
        return Optional.empty();
    }
}
