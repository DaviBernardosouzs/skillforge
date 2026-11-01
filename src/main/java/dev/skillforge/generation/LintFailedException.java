package dev.skillforge.generation;

import dev.skillforge.domain.LintReport;

/** A biblioteca tem violações ERROR e não pode ser gerada (vira 422 na API). */
public class LintFailedException extends RuntimeException {

    private final transient LintReport report;

    public LintFailedException(LintReport report) {
        super("A biblioteca tem " + report.errors().size() + " erro(s) de validação.");
        this.report = report;
    }

    public LintReport report() {
        return report;
    }
}
