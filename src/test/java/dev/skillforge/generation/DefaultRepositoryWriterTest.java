package dev.skillforge.generation;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import dev.skillforge.domain.Library;
import dev.skillforge.domain.License;
import dev.skillforge.domain.Skill;
import dev.skillforge.domain.SkillFile;
import dev.skillforge.domain.SkillGraphInfo;
import dev.skillforge.domain.SkillMetadata;
import dev.skillforge.domain.SkillName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static dev.skillforge.domain.Skills.skill;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultRepositoryWriterTest {

    private final DefaultRepositoryWriter writer = Generators.repositoryWriter();
    private FileSystem fs;
    private Path root;

    @BeforeEach
    void setUp() {
        fs = Jimfs.newFileSystem(Configuration.unix().toBuilder().setAttributeViews("basic", "posix").build());
        root = fs.getPath("/minhas-skills");
    }

    @AfterEach
    void tearDown() throws Exception {
        fs.close();
    }

    @Test
    void writesExactTree() throws Exception {
        Skill review = skill("revisao", "Veja [o checklist](references/checklist.md).", SkillGraphInfo.empty(),
                SkillFile.ofText("references/checklist.md", "- item"),
                SkillFile.ofText("scripts/run.sh", "#!/bin/sh"),
                new SkillFile("assets/logo.png", new byte[]{1, 2}));
        writer.write(new Library("minhas-skills", "Bernardo", License.MIT, List.of(review, skill("docs"))), root);

        assertThat(tree()).containsExactly(
                ".github/workflows/validate.yml",
                ".gitignore",
                "LICENSE",
                "README.md",
                "docs/COMO-CRIAR-UMA-SKILL.md",
                "scripts/validate.sh",
                "skills/docs/SKILL.md",
                "skills/revisao/SKILL.md",
                "skills/revisao/assets/logo.png",
                "skills/revisao/references/checklist.md",
                "skills/revisao/scripts/run.sh",
                "templates/SKILL.template.md");
        assertThat(root.resolve("skills/revisao/assets/logo.png")).hasBinaryContent(new byte[]{1, 2});
        assertThat(Files.getPosixFilePermissions(root.resolve("scripts/validate.sh"))).contains(PosixFilePermission.OWNER_EXECUTE);
        assertThat(Files.getPosixFilePermissions(root.resolve("skills/revisao/scripts/run.sh"))).contains(PosixFilePermission.OWNER_EXECUTE);
    }

    @Test
    void readmeListsAllSkillsAndLicense() throws Exception {
        writer.write(new Library("minhas-skills", "Bernardo", License.APACHE_2_0, List.of(skill("a"), skill("b"))), root);

        assertThat(root.resolve("README.md")).content()
                .contains("# minhas-skills", "**Bernardo**", "[a](skills/a/SKILL.md)", "[b](skills/b/SKILL.md)",
                        "Apache License 2.0", "## Abrir no Obsidian");
        assertThat(root.resolve("LICENSE")).content().contains("Apache License", "Copyright 2026 Bernardo");
    }

    @Test
    void omitsLicenseWhenNone() throws Exception {
        writer.write(new Library("minhas-skills", "Bernardo", License.NONE, List.of(skill("a"))), root);

        assertThat(root.resolve("LICENSE")).doesNotExist();
        assertThat(root.resolve("README.md")).content().doesNotContain("## Licença");
    }

    @Test
    void rewritesSkillMdCanonically() throws Exception {
        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("license", "MIT");
        extras.put("allowed-tools", "Bash");
        Skill skill = new Skill(new SkillMetadata(new SkillName("a"), "Faz A. Use quando pedirem A.", extras),
                "\n\n# A\n\ncorpo\n\n\n", List.of());

        writer.write(new Library("minhas-skills", "Bernardo", License.NONE, List.of(skill)), root);

        assertThat(root.resolve("skills/a/SKILL.md")).content().isEqualTo("""
                ---
                name: a
                description: Faz A. Use quando pedirem A.
                license: MIT
                allowed-tools: Bash
                ---

                # A

                corpo
                """);
    }

    @Test
    void escapesPipesInReadmeTable() {
        assertThat(DefaultRepositoryWriter.tableCell("a | b\nc")).isEqualTo("a \\| b c");
    }

    @ParameterizedTest
    @ValueSource(strings = {"../fora.md", "references/../../fora.md", "/etc/passwd", "outra/x.md"})
    void refusesUnsafePathsEvenIfLinterWasSkipped(String path) {
        Path skillDir = root.resolve("skills/a");
        assertThatThrownBy(() -> DefaultRepositoryWriter.safeResolve(skillDir, path))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private List<String> tree() throws Exception {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile).map(p -> root.relativize(p).toString()).sorted().toList();
        }
    }
}
