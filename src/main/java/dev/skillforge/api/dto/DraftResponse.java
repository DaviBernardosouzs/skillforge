package dev.skillforge.api.dto;

import dev.skillforge.authoring.DraftResult;
import dev.skillforge.authoring.QualityHint;

import java.util.List;

/** Resultado do assistente de redação. */
public record DraftResponse(String skillMd, List<StubFileResponse> stubFiles, LintReportResponse lint,
                            List<QualityHint> hints) {

    public static DraftResponse from(DraftResult result) {
        return new DraftResponse(result.skillMd(),
                result.stubFiles().stream().map(StubFileResponse::from).toList(),
                LintReportResponse.from(result.lint()),
                result.hints());
    }
}
