package dev.skillforge.domain;

import java.util.List;

/** Resultado do lint: todas as violações encontradas, de todas as regras. */
public record LintReport(List<Violation> violations) {

    public LintReport {
        violations = violations == null ? List.of() : List.copyOf(violations);
    }

    public static LintReport empty() {
        return new LintReport(List.of());
    }

    public boolean hasErrors() {
        return !errors().isEmpty();
    }

    public List<Violation> errors() {
        return bySeverity(Severity.ERROR);
    }

    public List<Violation> warnings() {
        return bySeverity(Severity.WARNING);
    }

    private List<Violation> bySeverity(Severity severity) {
        return violations.stream().filter(v -> v.severity() == severity).toList();
    }
}
