package dev.skillforge.graph.canvas;

import dev.skillforge.domain.GraphEdge;
import dev.skillforge.domain.GraphNode;
import dev.skillforge.domain.LibraryGraph;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converte o grafo num {@link Canvas}: um grupo por categoria (colunas lado a lado), um nó de
 * arquivo por skill apontando para o SKILL.md, e arestas para as relacionadas.
 * Os ids são derivados de hash, então a mesma entrada gera exatamente o mesmo arquivo.
 */
public final class CanvasLayout {

    static final int CARD_WIDTH = 400;
    static final int CARD_HEIGHT = 240;
    static final int GAP = 60;
    static final int GROUP_PADDING = 40;
    static final int GROUP_HEADER = 40;
    static final String UNCATEGORIZED = "Sem categoria";
    static final String GROUP_COLOR = "2";   // laranja
    static final String SKILL_COLOR = "1";   // vermelho

    private CanvasLayout() {
    }

    public static Canvas layout(LibraryGraph graph) {
        Map<String, List<GraphNode>> columns = new LinkedHashMap<>();
        for (GraphNode category : graph.nodesOfType(GraphNode.Type.CATEGORY)) {
            columns.put(category.label(), new ArrayList<>());
        }
        for (GraphNode skill : graph.nodesOfType(GraphNode.Type.SKILL)) {
            String key = skill.category() == null ? UNCATEGORIZED : skill.category();
            columns.computeIfAbsent(key, k -> new ArrayList<>()).add(skill);
        }

        List<CanvasNode> nodes = new ArrayList<>();
        Map<String, String> canvasIdBySkill = new LinkedHashMap<>();
        int columnX = 0;
        int groupWidth = CARD_WIDTH + 2 * GROUP_PADDING;
        for (Map.Entry<String, List<GraphNode>> column : columns.entrySet()) {
            List<GraphNode> skills = column.getValue();
            int groupHeight = GROUP_HEADER + GROUP_PADDING
                    + Math.max(1, skills.size()) * (CARD_HEIGHT + GAP) - GAP + GROUP_PADDING;
            nodes.add(CanvasNode.group(id("group:" + column.getKey()), columnX, 0, groupWidth, groupHeight,
                    column.getKey(), GROUP_COLOR));

            int cardY = GROUP_HEADER + GROUP_PADDING;
            for (GraphNode skill : skills) {
                String canvasId = id(skill.id());
                canvasIdBySkill.put(skill.id(), canvasId);
                nodes.add(CanvasNode.file(canvasId, columnX + GROUP_PADDING, cardY, CARD_WIDTH, CARD_HEIGHT,
                        skill.path(), SKILL_COLOR));
                cardY += CARD_HEIGHT + GAP;
            }
            columnX += groupWidth + GAP * 2;
        }

        List<CanvasEdge> edges = new ArrayList<>();
        for (GraphEdge edge : graph.edges()) {
            if (edge.type() == GraphEdge.Type.RELATED
                    && canvasIdBySkill.containsKey(edge.from()) && canvasIdBySkill.containsKey(edge.to())) {
                edges.add(new CanvasEdge(id(edge.from() + "->" + edge.to()), canvasIdBySkill.get(edge.from()),
                        "right", canvasIdBySkill.get(edge.to()), "left", "arrow", "relacionada"));
            }
        }
        return new Canvas(nodes, edges);
    }

    /** 16 caracteres hexadecimais derivados da chave (formato de id recomendado pelo JSON Canvas). */
    static String id(String key) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 sempre existe na JVM", e);
        }
    }

}
