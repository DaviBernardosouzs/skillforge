package dev.skillforge.generation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultZipPackagerTest {

    @TempDir
    Path temp;

    @Test
    void zipsFolderWithItselfAsRootIncludingHiddenFolders() throws Exception {
        Path root = temp.resolve("minhas-skills");
        Files.createDirectories(root.resolve(".git/refs"));
        Files.writeString(root.resolve(".git/HEAD"), "ref: refs/heads/main\n");
        Files.createDirectories(root.resolve("skills/a"));
        Files.writeString(root.resolve("skills/a/SKILL.md"), "conteúdo");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new DefaultZipPackager().zip(root, out);

        List<String> names = new ArrayList<>();
        String skillContent = null;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                names.add(entry.getName());
                if (entry.getName().endsWith("SKILL.md")) {
                    skillContent = new String(zip.readAllBytes());
                }
            }
        }
        assertThat(names).allMatch(n -> n.startsWith("minhas-skills/"))
                .contains("minhas-skills/.git/HEAD", "minhas-skills/.git/refs/", "minhas-skills/skills/a/SKILL.md");
        assertThat(skillContent).isEqualTo("conteúdo");
    }
}
