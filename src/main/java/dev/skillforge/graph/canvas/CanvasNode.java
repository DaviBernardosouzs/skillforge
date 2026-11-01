package dev.skillforge.graph.canvas;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Nó do JSON Canvas. Campos opcionais nulos não são escritos. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CanvasNode(String id, String type, int x, int y, int width, int height,
                         String file, String text, String label, String color) {

    public static CanvasNode group(String id, int x, int y, int width, int height, String label, String color) {
        return new CanvasNode(id, "group", x, y, width, height, null, null, label, color);
    }

    public static CanvasNode file(String id, int x, int y, int width, int height, String file, String color) {
        return new CanvasNode(id, "file", x, y, width, height, file, null, null, color);
    }

    public static CanvasNode text(String id, int x, int y, int width, int height, String text) {
        return new CanvasNode(id, "text", x, y, width, height, null, text, null, null);
    }
}
