package dev.skillforge.generation;

import dev.skillforge.domain.License;
import dev.skillforge.domain.SkillFile;
import dev.skillforge.domain.SkillGraphInfo;
import dev.skillforge.domain.Violation;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LibraryGeneratorTest {

    static final String VALID = """
            ---
            name: revisao
            description: Revisa PRs. Use quando o usuário pedir revisão.
            ---
            # Revisão

            Siga o [checklist](references/checklist.md).
            """;

    @Test
    void generatesZipWithRepositoryVaultAndGit() throws Exception {
        LibraryInput input = new LibraryInput("minhas-skills", "Bernardo", License.MIT, List.of(
                new SkillInput(VALID, List.of(SkillFile.ofText("references/checklist.md", "- a")),
                        new SkillGraphInfo("dev", List.of("java"), List.of()))));

        GeneratedLibrary result = Generators.generator().generate(input);

        assertThat(result.fileName()).isEqualTo("minhas-skills.zip");
        assertThat(entries(result.zip())).contains(
                "minhas-skills/README.md", "minhas-skills/skills/revisao/SKILL.md",
                "minhas-skills/skills/revisao/references/checklist.md", "minhas-skills/.git/HEAD",
                "minhas-skills/_graph/skills/revisao.md", "minhas-skills/biblioteca.canvas");
    }

    @Test
    void reportsAllErrorsAtOnceAndWritesNothing() {
        String brokenName = VALID.replace("name: revisao", "name: Revisão Ruim");
        LibraryInput input = new LibraryInput("minhas-skills", "Bernardo", License.MIT, List.of(
                new SkillInput(brokenName, List.of(SkillFile.ofText("../fora.md", "x")), null)));

        assertThatThrownBy(() -> Generators.generator().generate(input))
                .isInstanceOfSatisfying(LintFailedException.class, e -> assertThat(e.report().errors())
                        .extracting(Violation::ruleId)
                        .containsExactlyInAnyOrder("name-format", "references-exist", "file-path"));
    }

    @Test
    void deletesTempFolderEvenWhenWritingFails() {
        AtomicReference<Path> usedRoot = new AtomicReference<>();
        RepositoryWriter failing = (library, root) -> {
            usedRoot.set(root);
            Files.writeString(root.resolve("parcial.txt"), "x");
            throw new IOException("disco cheio");
        };
        LibraryInput input = new LibraryInput("minhas-skills", "Bernardo", License.NONE,
                List.of(new SkillInput(VALID.replace("Siga o [checklist](references/checklist.md).", ""), List.of(), null)));

        assertThatThrownBy(() -> Generators.generator(failing).generate(input)).hasRootCauseMessage("disco cheio");
        assertThat(usedRoot.get().getParent()).doesNotExist();
    }

    static List<String> entries(byte[] zipBytes) throws IOException {
        List<String> names = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                names.add(entry.getName());
            }
        }
        return names;
    }
}
