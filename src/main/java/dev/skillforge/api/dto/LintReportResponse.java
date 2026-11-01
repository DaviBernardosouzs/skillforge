package dev.skillforge.api.dto;

import dev.skillforge.domain.LintReport;
import dev.skillforge.domain.Violation;

import java.util.List;

/** Relatório do linter com contagens prontas para a interface. */
public record LintReportResponse(List<Violation> violations, int errorCount, int warningCount, boolean hasErrors) {

    public static LintReportResponse from(LintReport report) {
        return new LintReportResponse(report.violations(), report.errors().size(), report.warnings().size(),
                report.hasErrors());
    }
}
