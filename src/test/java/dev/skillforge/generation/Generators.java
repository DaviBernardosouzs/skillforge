package dev.skillforge.generation;

import dev.skillforge.config.SkillLimits;
import dev.skillforge.graph.DefaultObsidianVaultWriter;
import dev.skillforge.graph.LibraryGraphBuilder;
import dev.skillforge.parsing.FrontmatterSplitter;
import dev.skillforge.parsing.SkillParser;
import dev.skillforge.template.SkillMarkdownWriter;
import dev.skillforge.template.TemplateRenderer;
import dev.skillforge.validation.Linters;
import dev.skillforge.validation.SizeGuard;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** Monta as peças da geração sem Spring, para testes. */
public final class Generators {

    public static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-11-01T12:00:00Z"), ZoneOffset.UTC);

    private Generators() {
    }

    public static DefaultRepositoryWriter repositoryWriter() {
        return new DefaultRepositoryWriter(new TemplateRenderer(), new SkillMarkdownWriter(), SkillLimits.defaults(), FIXED_CLOCK);
    }

    public static LibraryAnalyzer analyzer() {
        return new LibraryAnalyzer(new SizeGuard(SkillLimits.defaults()), new SkillParser(new FrontmatterSplitter()),
                Linters.defaultLinter());
    }

    public static LibraryGenerator generator(RepositoryWriter writer) {
        return new LibraryGenerator(analyzer(), writer, new LibraryGraphBuilder(),
                new DefaultObsidianVaultWriter(new TemplateRenderer(), SkillLimits.defaults()),
                new JGitInitializer(), new DefaultZipPackager());
    }

    public static LibraryGenerator generator() {
        return generator(repositoryWriter());
    }
}
