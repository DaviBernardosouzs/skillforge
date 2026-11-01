package dev.skillforge.authoring;

import dev.skillforge.authoring.SkillDraft.ResourceKind;
import dev.skillforge.authoring.SkillDraft.ResourcePlan;
import dev.skillforge.config.SkillLimits;
import dev.skillforge.domain.LintReport;
import dev.skillforge.domain.SkillFile;
import dev.skillforge.parsing.SkillParser;
import dev.skillforge.template.SkillMarkdownWriter;
import dev.skillforge.template.SkillTitles;
import dev.skillforge.template.TemplateRenderer;
import dev.skillforge.validation.SkillLinter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

/**
 * Transforma as respostas do formulário ({@link SkillDraft}) num SKILL.md bem estruturado,
 * com arquivos-esqueleto para os recursos planejados, e passa o resultado pelo linter.
 */
@Component
public class SkillDraftComposer {

    private static final String TEMPLATE = "authoring/SKILL.draft.md";
    private static final String ELLIPSIS = "…";

    private final TemplateRenderer renderer;
    private final SkillMarkdownWriter markdownWriter;
    private final SkillParser parser;
    private final SkillLinter linter;
    private final DraftQualityAdvisor advisor;
    private final SkillLimits limits;

    public SkillDraftComposer(TemplateRenderer renderer, SkillMarkdownWriter markdownWriter, SkillParser parser,
                              SkillLinter linter, DraftQualityAdvisor advisor, SkillLimits limits) {
        this.renderer = renderer;
        this.markdownWriter = markdownWriter;
        this.parser = parser;
        this.linter = linter;
        this.advisor = advisor;
        this.limits = limits;
    }

    public DraftResult compose(SkillDraft draft) {
        String fullDescription = describe(draft);
        String description = truncate(fullDescription, limits.descriptionMaxLength());
        boolean truncated = !description.equals(fullDescription);

        String body = renderer.render(TEMPLATE, context(draft));
        String skillMd = markdownWriter.write(draft.name().strip(), description, Map.of(), body);
        List<SkillFile> stubs = draft.resources().stream().map(SkillDraftComposer::stub).toList();

        LintReport lint = linter.lintParsed(parser.parse(skillMd, stubs));
        long bodyLines = body.strip().lines().count();
        List<QualityHint> hints = advisor.advise(new DraftAnalysis(draft, truncated, bodyLines, limits));
        return new DraftResult(skillMd, stubs, lint, hints);
    }

    /** "o que faz + quando usar": "{purpose} Use quando {trigger1}, {trigger2}." */
    static String describe(SkillDraft draft) {
        String triggers = String.join(", ", draft.triggers().stream().map(SkillDraftComposer::stripEndPunctuation).toList());
        return endWithPeriod(draft.purpose().strip()) + " Use quando " + triggers + ".";
    }

    /** Corta no último espaço antes do limite e termina com reticências. */
    static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        String cut = text.substring(0, maxLength - ELLIPSIS.length());
        int lastSpace = cut.lastIndexOf(' ');
        if (lastSpace > maxLength / 2) {
            cut = cut.substring(0, lastSpace);
        }
        return stripEndPunctuation(cut) + ELLIPSIS;
    }

    private static Map<String, Object> context(SkillDraft draft) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("title", SkillTitles.fromName(draft.name()));
        ctx.put("purpose", endWithPeriod(draft.purpose().strip()));
        ctx.put("triggers", draft.triggers());
        putList(ctx, "antiTriggers", "hasAntiTriggers", draft.antiTriggers());
        putList(ctx, "inputs", "hasInputs", draft.inputs());
        ctx.put("steps", numbered(draft.steps().stream()
                .map(step -> Map.<String, Object>of("title", step.title().strip(), "detail", step.detail()))
                .toList()));
        ctx.put("outputFormat", draft.outputFormat());
        putList(ctx, "examples", "hasExamples", numbered(draft.examples().stream()
                .map(e -> Map.<String, Object>of("input", e.input().strip(), "expectedOutput", e.expectedOutput().strip()))
                .toList()));
        putList(ctx, "pitfalls", "hasPitfalls", draft.pitfalls());
        putList(ctx, "qualityChecks", "hasQualityChecks", draft.qualityChecks());
        putList(ctx, "resources", "hasResources", draft.resources().stream()
                .map(r -> Map.<String, Object>of("fileName", fileName(r), "path", path(r), "purpose", r.purpose()))
                .toList());
        return ctx;
    }

    private static void putList(Map<String, Object> ctx, String key, String flag, List<?> items) {
        ctx.put(key, items);
        ctx.put(flag, !items.isEmpty());
    }

    /** Acrescenta "number" (1, 2, 3...) em cada item, para listas numeradas no template. */
    private static List<Map<String, Object>> numbered(List<Map<String, Object>> items) {
        return IntStream.range(0, items.size()).mapToObj(i -> {
            Map<String, Object> item = new HashMap<>(items.get(i));
            item.put("number", i + 1);
            return item;
        }).toList();
    }

    // ---- arquivos-esqueleto ----

    static SkillFile stub(ResourcePlan plan) {
        String purpose = plan.purpose().isBlank() ? "Descreva aqui o propósito deste arquivo." : plan.purpose();
        String content = switch (plan.kind()) {
            case REFERENCE -> "# " + titleOf(fileName(plan)) + "\n\n" + purpose + "\n";
            case SCRIPT -> scriptStub(fileName(plan), purpose);
            case ASSET -> "";
        };
        return SkillFile.ofText(path(plan), content);
    }

    private static String scriptStub(String fileName, String purpose) {
        Entry<String, String> interpreter = fileName.endsWith(".py") ? Map.entry("python3", "#")
                : fileName.endsWith(".js") || fileName.endsWith(".mjs") ? Map.entry("node", "//")
                : Map.entry("bash", "#");
        String comment = interpreter.getValue();
        List<String> lines = new ArrayList<>();
        lines.add("#!/usr/bin/env " + interpreter.getKey());
        lines.add(comment + " " + purpose);
        lines.add(comment + " TODO: implementar.");
        if (interpreter.getKey().equals("bash")) {
            lines.add("set -euo pipefail");
        }
        return String.join("\n", lines) + "\n";
    }

    /** Referências sem extensão viram .md. */
    private static String fileName(ResourcePlan plan) {
        String name = plan.fileName().strip();
        return plan.kind() == ResourceKind.REFERENCE && !name.contains(".") ? name + ".md" : name;
    }

    private static String path(ResourcePlan plan) {
        return plan.kind().folder() + "/" + fileName(plan);
    }

    private static String titleOf(String fileName) {
        String base = fileName.contains("/") ? fileName.substring(fileName.lastIndexOf('/') + 1) : fileName;
        base = base.contains(".") ? base.substring(0, base.lastIndexOf('.')) : base;
        return SkillTitles.fromName(base.replace('_', '-'));
    }

    // ---- texto ----

    private static String endWithPeriod(String text) {
        return text.matches(".*[.!?…]$") ? text : text + ".";
    }

    private static String stripEndPunctuation(String text) {
        return text.strip().replaceAll("[\\s.,;:!?…]+$", "");
    }
}
