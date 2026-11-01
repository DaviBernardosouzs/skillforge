package dev.skillforge.authoring;

import dev.skillforge.domain.LintReport;
import dev.skillforge.domain.SkillFile;

import java.util.List;

/** O SKILL.md gerado, os arquivos-esqueleto, o resultado do linter e as dicas de qualidade. */
public record DraftResult(String skillMd, List<SkillFile> stubFiles, LintReport lint, List<QualityHint> hints) {

    public DraftResult {
        stubFiles = List.copyOf(stubFiles);
        hints = List.copyOf(hints);
    }
}
