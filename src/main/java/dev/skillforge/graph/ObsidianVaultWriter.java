package dev.skillforge.graph;

import dev.skillforge.domain.LibraryGraph;

import java.io.IOException;
import java.nio.file.Path;

/** Escreve a camada Obsidian (.obsidian/, _graph/, _templates/, canvas) dentro do repositório. */
public interface ObsidianVaultWriter {

    void write(LibraryGraph graph, Path root) throws IOException;
}
