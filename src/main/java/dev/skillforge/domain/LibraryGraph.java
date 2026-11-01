package dev.skillforge.domain;

import java.util.List;

/** Modelo puro do grafo da biblioteca: vira notas do Obsidian, canvas e o JSON de pré-visualização. */
public record LibraryGraph(List<GraphNode> nodes, List<GraphEdge> edges) {

    public LibraryGraph {
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
    }

    public List<GraphNode> nodesOfType(GraphNode.Type type) {
        return nodes.stream().filter(n -> n.type() == type).toList();
    }

    public List<GraphEdge> edgesFrom(String nodeId) {
        return edges.stream().filter(e -> e.from().equals(nodeId)).toList();
    }
}
