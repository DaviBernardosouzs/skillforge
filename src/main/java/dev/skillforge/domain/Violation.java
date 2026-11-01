package dev.skillforge.domain;

/** Um problema encontrado pelo linter. {@code skill} é o nome (ou a posição) da skill afetada. */
public record Violation(String ruleId, Severity severity, String skill, String message) {

    public static Violation error(String ruleId, String skill, String message) {
        return new Violation(ruleId, Severity.ERROR, skill, message);
    }

    public static Violation warning(String ruleId, String skill, String message) {
        return new Violation(ruleId, Severity.WARNING, skill, message);
    }
}
