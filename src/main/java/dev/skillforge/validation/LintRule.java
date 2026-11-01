package dev.skillforge.validation;

import dev.skillforge.domain.RawSkill;
import dev.skillforge.domain.Violation;

import java.util.List;

/** Uma regra do linter (padrão Strategy): regra nova = classe nova. */
public interface LintRule {

    String id();

    List<Violation> check(RawSkill skill, LintContext ctx);
}
