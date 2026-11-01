package dev.skillforge.generation;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/** Compacta a pasta (incluindo .git/) com ela mesma como raiz do zip. */
public interface ZipPackager {

    void zip(Path root, OutputStream out) throws IOException;
}
