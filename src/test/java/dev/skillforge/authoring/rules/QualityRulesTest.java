package dev.skillforge.authoring.rules;

import dev.skillforge.authoring.DraftAnalysis;
import dev.skillforge.authoring.Drafts;
import dev.skillforge.authoring.QualityRule;
import dev.skillforge.authoring.SkillDraft;
import dev.skillforge.authoring.SkillDraft.Step;
import dev.skillforge.config.SkillLimits;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Cada regra com um caso que dispara e um que não dispara. O rascunho completo não dispara nenhuma. */
class QualityRulesTest {

    private static final SkillDraft COMPLETE = Drafts.complete();
    private static final SkillDraft MINIMAL = Drafts.minimal();

    @Test
    void fewTriggers() {
        assertFires(new FewTriggersRule(), analysis(MINIMAL));
        assertSilent(new FewTriggersRule(), analysis(COMPLETE));
    }

    @Test
    void noAntiTriggers() {
        assertFires(new NoAntiTriggersRule(), analysis(MINIMAL));
        assertSilent(new NoAntiTriggersRule(), analysis(COMPLETE));
    }

    @Test
    void noExamples() {
        assertFires(new NoExamplesRule(), analysis(MINIMAL));
        assertSilent(new NoExamplesRule(), analysis(COMPLETE));
    }

    @Test
    void vagueSteps() {
        SkillDraft vague = new SkillDraft("a", "p", List.of("t"), null, null,
                List.of(new Step("Revisar", "revisar o código")), null, null, null, null, null);
        assertFires(new VagueStepsRule(), analysis(vague));
        assertSilent(new VagueStepsRule(), analysis(COMPLETE));
    }

    @Test
    void noOutputFormat() {
        assertFires(new NoOutputFormatRule(), analysis(MINIMAL));
        assertSilent(new NoOutputFormatRule(), analysis(COMPLETE));
    }

    @Test
    void noQualityChecks() {
        assertFires(new NoQualityChecksRule(), analysis(MINIMAL));
        assertSilent(new NoQualityChecksRule(), analysis(COMPLETE));
    }

    @Test
    void descriptionTruncated() {
        var limits = SkillLimits.defaults();
        assertFires(new DescriptionTruncatedRule(), new DraftAnalysis(COMPLETE, true, 10, limits));
        assertSilent(new DescriptionTruncatedRule(), new DraftAnalysis(COMPLETE, false, 10, limits));
    }

    @Test
    void longBody() {
        var limits = SkillLimits.defaults();
        assertFires(new LongBodyRule(), new DraftAnalysis(COMPLETE, false, 501, limits));
        assertSilent(new LongBodyRule(), new DraftAnalysis(COMPLETE, false, 500, limits));
    }

    private static DraftAnalysis analysis(SkillDraft draft) {
        return new DraftAnalysis(draft, false, 20, SkillLimits.defaults());
    }

    private static void assertFires(QualityRule rule, DraftAnalysis analysis) {
        assertThat(rule.check(analysis)).hasValueSatisfying(hint -> {
            assertThat(hint.id()).isEqualTo(rule.id());
            assertThat(hint.message()).isNotBlank();
        });
    }

    private static void assertSilent(QualityRule rule, DraftAnalysis analysis) {
        assertThat(rule.check(analysis)).isEmpty();
    }
}
