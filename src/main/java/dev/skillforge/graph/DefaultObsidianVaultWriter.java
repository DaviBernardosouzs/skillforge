package dev.skillforge.graph;

import dev.skillforge.config.SkillLimits;
import dev.skillforge.domain.GraphEdge;
import dev.skillforge.domain.GraphNode;
import dev.skillforge.domain.LibraryGraph;
import dev.skillforge.graph.canvas.CanvasLayout;
import dev.skillforge.template.TemplateRenderer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Escreve o vault Obsidian a partir do {@link LibraryGraph}, usando os templates de
 * {@code templates/obsidian/}. O canvas é serializado com Jackson a partir de records.
 */
@Component
public class DefaultObsidianVaultWriter implements ObsidianVaultWriter {

    static final String INDEX_NOTE = "_graph/00 - Indice.md";
    static final String CANVAS_FILE = "biblioteca.canvas";
    static final String NEW_SKILL_TEMPLATE = "_templates/Nova skill.md";

    private final TemplateRenderer renderer;
    private final SkillLimits limits;
    private final JsonMapper json = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    public DefaultObsidianVaultWriter(TemplateRenderer renderer, SkillLimits limits) {
        this.renderer = renderer;
        this.limits = limits;
    }

    @Override
    public void write(LibraryGraph graph, Path root) throws IOException {
        String libraryName = graph.nodesOfType(GraphNode.Type.INDEX).getFirst().label();

        writeText(root.resolve(".obsidian/app.json"), renderer.render("obsidian/app.json", Map.of()));
        writeText(root.resolve(".obsidian/graph.json"), renderer.render("obsidian/graph.json", Map.of()));
        writeText(root.resolve(".obsidian/templates.json"), renderer.render("obsidian/templates.json", Map.of()));

        writeText(root.resolve(INDEX_NOTE), renderer.render("obsidian/index-note.md", indexContext(graph, libraryName)));
        for (GraphNode category : graph.nodesOfType(GraphNode.Type.CATEGORY)) {
            writeText(root.resolve("_graph/categorias/" + NoteNames.sanitize(category.label()) + ".md"),
                    renderer.render("obsidian/category-note.md", categoryContext(graph, category, libraryName)));
        }
        for (GraphNode skill : graph.nodesOfType(GraphNode.Type.SKILL)) {
            writeText(root.resolve("_graph/skills/" + NoteNames.sanitize(skill.label()) + ".md"),
                    renderer.render("obsidian/skill-note.md", skillContext(graph, skill, libraryName)));
        }

        writeText(root.resolve(NEW_SKILL_TEMPLATE), renderer.render("repo/SKILL.template.md", Map.of(
                "nameMaxLength", limits.nameMaxLength(),
                "descriptionMaxLength", limits.descriptionMaxLength(),
                "bodyMaxLines", limits.bodyMaxLines())));
        writeText(root.resolve(CANVAS_FILE), json.writeValueAsString(CanvasLayout.layout(graph)) + "\n");
    }

    private Map<String, Object> indexContext(LibraryGraph graph, String libraryName) {
        List<Map<String, String>> categories = graph.nodesOfType(GraphNode.Type.CATEGORY).stream()
                .map(c -> Map.of("note", NoteNames.sanitize(c.label()), "label", c.label()))
                .toList();
        List<String> uncategorized = graph.nodesOfType(GraphNode.Type.SKILL).stream()
                .filter(s -> s.category() == null)
                .map(GraphNode::label)
                .toList();
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("libraryName", libraryName);
        ctx.put("categories", categories);
        ctx.put("hasCategories", !categories.isEmpty());
        ctx.put("uncategorized", uncategorized);
        ctx.put("hasUncategorized", !uncategorized.isEmpty());
        return ctx;
    }

    private static Map<String, Object> categoryContext(LibraryGraph graph, GraphNode category, String libraryName) {
        List<String> skills = graph.edgesFrom(category.id()).stream()
                .map(GraphEdge::to)
                .map(id -> id.substring("skill:".length()))
                .toList();
        return Map.of("category", category.label(), "libraryName", libraryName, "skills", skills);
    }

    private Map<String, Object> skillContext(LibraryGraph graph, GraphNode skill, String libraryName) {
        List<Map<String, String>> files = new ArrayList<>();
        List<String> related = new ArrayList<>();
        List<String> mentions = new ArrayList<>();
        for (GraphEdge edge : graph.edgesFrom(skill.id())) {
            switch (edge.type()) {
                case HAS_FILE -> graph.nodes().stream().filter(n -> n.id().equals(edge.to())).findFirst()
                        .ifPresent(file -> files.add(Map.of("link", wikilinkTarget(file.path()), "label", file.label())));
                case RELATED -> related.add(skillName(edge.to()));
                case MENTIONS -> mentions.add(skillName(edge.to()));
                case CONTAINS -> { /* skill não contém outros nós */ }
            }
        }

        Set<String> tags = new LinkedHashSet<>();
        tags.add("skill");
        skill.tags().stream().map(NoteNames::tag).filter(t -> !t.isEmpty()).forEach(tags::add);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("name", skill.label());
        ctx.put("description", skill.description().replace("\n", " "));
        ctx.put("libraryName", libraryName);
        ctx.put("tags", List.copyOf(tags));
        if (skill.category() != null) {
            ctx.put("category", skill.category());
            ctx.put("categoryYaml", json.writeValueAsString(skill.category()));
            ctx.put("categoryNote", NoteNames.sanitize(skill.category()));
        }
        ctx.put("files", files);
        ctx.put("hasFiles", !files.isEmpty());
        ctx.put("related", related);
        ctx.put("hasRelated", !related.isEmpty());
        ctx.put("mentions", mentions);
        ctx.put("hasMentions", !mentions.isEmpty());
        return ctx;
    }

    /** Notas .md são linkadas sem extensão; outros arquivos (scripts) com a extensão. */
    private static String wikilinkTarget(String path) {
        return path.endsWith(".md") ? path.substring(0, path.length() - 3) : path;
    }

    private static String skillName(String nodeId) {
        return nodeId.substring("skill:".length());
    }

    private static void writeText(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }
}
