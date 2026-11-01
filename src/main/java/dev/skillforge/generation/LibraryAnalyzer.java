package dev.skillforge.generation;

import dev.skillforge.domain.LintReport;
import dev.skillforge.domain.RawSkill;
import dev.skillforge.parsing.ParseResult;
import dev.skillforge.parsing.SkillParser;
import dev.skillforge.validation.SizeGuard;
import dev.skillforge.validation.SkillLinter;
import org.springframework.stereotype.Component;

import java.util.List;

/** Etapas comuns a lint, grafo e geração: conferir limites, fazer o parse e rodar o linter. */
@Component
public class LibraryAnalyzer {

    /** O que foi lido com sucesso e o relatório de todas as skills. */
    public record Analysis(List<RawSkill> parsed, LintReport report) {
    }

    private final SizeGuard sizeGuard;
    private final SkillParser parser;
    private final SkillLinter linter;

    public LibraryAnalyzer(SizeGuard sizeGuard, SkillParser parser, SkillLinter linter) {
        this.sizeGuard = sizeGuard;
        this.parser = parser;
        this.linter = linter;
    }

    /** @throws dev.skillforge.validation.LimitExceededException se a biblioteca passar dos limites */
    public Analysis analyze(List<SkillInput> skills) {
        sizeGuard.check(skills.stream().map(SkillInput::content).toList(),
                skills.stream().map(SkillInput::files).toList());

        List<ParseResult> results = skills.stream()
                .map(input -> withGraph(parser.parse(input.content(), input.files()), input))
                .toList();
        List<RawSkill> parsed = results.stream()
                .filter(ParseResult.Success.class::isInstance)
                .map(r -> ((ParseResult.Success) r).skill())
                .toList();
        return new Analysis(parsed, linter.lintParsed(results));
    }

    private static ParseResult withGraph(ParseResult result, SkillInput input) {
        return switch (result) {
            case ParseResult.Success success -> new ParseResult.Success(success.skill().withGraph(input.graph()));
            case ParseResult.Failure failure -> failure;
        };
    }
}
