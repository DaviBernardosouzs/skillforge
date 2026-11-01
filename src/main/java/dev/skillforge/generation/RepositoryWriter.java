package dev.skillforge.generation;

import dev.skillforge.domain.Library;

import java.io.IOException;
import java.nio.file.Path;

/** Escreve a árvore de arquivos do repositório. Recebe {@link Path} para funcionar com Jimfs nos testes. */
public interface RepositoryWriter {

    void write(Library library, Path root) throws IOException;
}
