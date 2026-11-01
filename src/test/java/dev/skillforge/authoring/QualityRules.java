package dev.skillforge.authoring;

import dev.skillforge.authoring.rules.DescriptionTruncatedRule;
import dev.skillforge.authoring.rules.FewTriggersRule;
import dev.skillforge.authoring.rules.LongBodyRule;
import dev.skillforge.authoring.rules.NoAntiTriggersRule;
import dev.skillforge.authoring.rules.NoExamplesRule;
import dev.skillforge.authoring.rules.NoOutputFormatRule;
import dev.skillforge.authoring.rules.NoQualityChecksRule;
import dev.skillforge.authoring.rules.VagueStepsRule;

import java.util.List;

/** Todas as regras de qualidade, sem Spring, para testes. */
public final class QualityRules {

    private QualityRules() {
    }

    public static List<QualityRule> all() {
        return List.of(new FewTriggersRule(), new NoAntiTriggersRule(), new NoExamplesRule(), new VagueStepsRule(),
                new NoOutputFormatRule(), new NoQualityChecksRule(), new DescriptionTruncatedRule(), new LongBodyRule());
    }
}
