package dev.skillforge.graph;

import dev.skillforge.domain.GraphEdge;
import dev.skillforge.domain.GraphNode;
import dev.skillforge.domain.Library;
import dev.skillforge.domain.LibraryGraph;
import dev.skillforge.domain.Skill;
import dev.skillforge.domain.SkillFile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Monta o {@link LibraryGraph} a partir das skills. É puro (sem IO): mesma entrada, mesmo grafo.
 */
@Component
public class LibraryGraphBuilder {

    public static final String INDEX_ID = "index";

    public LibraryGraph build(Library library) {
        return build(library.name(), library.skills());
    }

    public LibraryGraph build(String libraryName, List<Skill> skills) {
        List<GraphNode> nodes = new ArrayList<>();
        Set<GraphEdge> edges = new LinkedHashSet<>(); // evita arestas repetidas e mantém a ordem

        nodes.add(new GraphNode(INDEX_ID, GraphNode.Type.INDEX, libraryName, null, null, null, List.of()));

        Set<String> categories = new LinkedHashSet<>();
        for (Skill skill : skills) {
            String category = skill.graph().category();
            if (category != null && categories.add(category)) {
                nodes.add(new GraphNode(categoryId(category), GraphNode.Type.CATEGORY, category,
                        null, null, null, List.of()));
                edges.add(new GraphEdge(INDEX_ID, categoryId(category), GraphEdge.Type.CONTAINS));
            }
        }

        Set<String> names = new LinkedHashSet<>(skills.stream().map(Skill::name).toList());
        for (Skill skill : skills) {
            addSkill(skill, names, nodes, edges);
        }
        return new LibraryGraph(nodes, new ArrayList<>(edges));
    }

    private static void addSkill(Skill skill, Set<String> names, List<GraphNode> nodes, Set<GraphEdge> edges) {
        String id = skillId(skill.name());
        String category = skill.graph().category();
        nodes.add(new GraphNode(id, GraphNode.Type.SKILL, skill.name(), "skills/" + skill.name() + "/SKILL.md",
                skill.metadata().description(), category, skill.graph().tags()));
        edges.add(new GraphEdge(category == null ? INDEX_ID : categoryId(category), id, GraphEdge.Type.CONTAINS));

        for (SkillFile file : skill.files()) {
            GraphNode.Type type = fileType(file.relativePath());
            if (type != null) {
                String fileId = fileId(skill.name(), file.relativePath());
                nodes.add(new GraphNode(fileId, type, file.relativePath(),
                        "skills/" + skill.name() + "/" + file.relativePath(), null, null, List.of()));
                edges.add(new GraphEdge(id, fileId, GraphEdge.Type.HAS_FILE));
            }
        }

        for (String related : skill.graph().related()) {
            if (names.contains(related) && !related.equals(skill.name())) {
                edges.add(new GraphEdge(id, skillId(related), GraphEdge.Type.RELATED));
            }
        }

        for (String other : names) {
            if (!other.equals(skill.name()) && mentions(skill.body(), other)) {
                edges.add(new GraphEdge(id, skillId(other), GraphEdge.Type.MENTIONS));
            }
        }
    }

    /** O name aparece como palavra inteira (não como parte de outro nome kebab-case). */
    static boolean mentions(String body, String name) {
        return Pattern.compile("(?<![a-z0-9-])" + Pattern.quote(name) + "(?![a-z0-9-])").matcher(body).find();
    }

    private static GraphNode.Type fileType(String relativePath) {
        if (relativePath.startsWith("references/")) {
            return GraphNode.Type.REFERENCE;
        }
        if (relativePath.startsWith("scripts/")) {
            return GraphNode.Type.SCRIPT;
        }
        return null; // assets/ não entram no grafo
    }

    public static String skillId(String name) {
        return "skill:" + name;
    }

    public static String categoryId(String category) {
        return "category:" + category;
    }

    public static String fileId(String skill, String relativePath) {
        return "file:" + skill + "/" + relativePath;
    }
}
