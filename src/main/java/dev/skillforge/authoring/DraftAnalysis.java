package dev.skillforge.authoring;

import dev.skillforge.config.SkillLimits;

/** O que as {@link QualityRule} podem consultar: o rascunho e fatos sobre o SKILL.md gerado. */
public record DraftAnalysis(SkillDraft draft, boolean descriptionTruncated, long bodyLines, SkillLimits limits) {
}
