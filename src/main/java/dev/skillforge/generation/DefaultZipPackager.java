package dev.skillforge.generation;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Zip com java.util.zip. A pasta da biblioteca é a raiz do zip ("minhas-skills/README.md").
 * O java.util.zip não grava permissões Unix, por isso o README gerado ensina chmod +x.
 */
@Component
public class DefaultZipPackager implements ZipPackager {

    @Override
    public void zip(Path root, OutputStream out) throws IOException {
        String rootName = root.getFileName().toString();
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(root)) {
            paths = walk.sorted().toList(); // ordem estável
        }

        ZipOutputStream zip = new ZipOutputStream(out);
        for (Path path : paths) {
            String relative = root.relativize(path).toString().replace('\\', '/');
            String entryName = relative.isEmpty() ? rootName + "/" : rootName + "/" + relative;
            if (Files.isDirectory(path)) {
                zip.putNextEntry(new ZipEntry(entryName.endsWith("/") ? entryName : entryName + "/"));
            } else {
                zip.putNextEntry(new ZipEntry(entryName));
                Files.copy(path, zip);
            }
            zip.closeEntry();
        }
        zip.finish(); // não fecha o OutputStream de quem chamou
    }
}
