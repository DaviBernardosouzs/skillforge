package dev.skillforge.graph.canvas;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Aresta do JSON Canvas. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CanvasEdge(String id, String fromNode, String fromSide, String toNode, String toSide,
                         String toEnd, String label) {
}
