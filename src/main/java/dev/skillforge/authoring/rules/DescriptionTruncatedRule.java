package dev.skillforge.authoring.rules;

import dev.skillforge.authoring.DraftAnalysis;
import dev.skillforge.authoring.QualityHint;
import dev.skillforge.authoring.QualityRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** A description gerada precisou ser cortada para caber no limite. */
@Component
public class DescriptionTruncatedRule implements QualityRule {

    public static final String ID = "description-truncated";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Optional<QualityHint> check(DraftAnalysis analysis) {
        if (analysis.descriptionTruncated()) {
            return Optional.of(new QualityHint(ID, "A description passou de " + analysis.limits().descriptionMaxLength() + " caracteres e foi cortada. Resuma o propósito ou junte situações de uso parecidas."));
        }
        return Optional.empty();
    }
}
