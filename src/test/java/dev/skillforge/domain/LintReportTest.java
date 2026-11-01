package dev.skillforge.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LintReportTest {

    @Test
    void separatesErrorsFromWarnings() {
        Violation error = Violation.error("name-required", "x", "falta name");
        Violation warning = Violation.warning("body-not-empty", "x", "corpo vazio");

        LintReport report = new LintReport(List.of(error, warning));

        assertThat(report.hasErrors()).isTrue();
        assertThat(report.errors()).containsExactly(error);
        assertThat(report.warnings()).containsExactly(warning);
    }

    @Test
    void onlyWarningsMeansNoErrors() {
        LintReport report = new LintReport(List.of(Violation.warning("body-length", "x", "longo")));

        assertThat(report.hasErrors()).isFalse();
        assertThat(LintReport.empty().hasErrors()).isFalse();
    }
}
