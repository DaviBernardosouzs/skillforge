package dev.skillforge.graph.canvas;

import java.util.List;

/** Arquivo .canvas no formato JSON Canvas 1.0 (jsoncanvas.org). */
public record Canvas(List<CanvasNode> nodes, List<CanvasEdge> edges) {
}
