package dev.skillforge.graph;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import dev.skillforge.config.SkillLimits;
import dev.skillforge.domain.LibraryGraph;
import dev.skillforge.domain.SkillFile;
import dev.skillforge.domain.SkillGraphInfo;
import dev.skillforge.template.TemplateRenderer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static dev.skillforge.domain.Skills.graph;
import static dev.skillforge.domain.Skills.skill;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultObsidianVaultWriterTest {

    private final DefaultObsidianVaultWriter writer = new DefaultObsidianVaultWriter(new TemplateRenderer(), SkillLimits.defaults());
    private final LibraryGraph graph = new LibraryGraphBuilder().build("minhas", List.of(
            skill("revisao", "Rode gerar-testes.", graph("Dev: Java", List.of("java", "code review"), List.of("gerar-testes")),
                    SkillFile.ofText("references/checklist.md", "x"), SkillFile.ofText("scripts/run.sh", "x")),
            skill("gerar-testes", "corpo", graph("Dev: Java", List.of(), List.of())),
            skill("docs", "corpo", SkillGraphInfo.empty())));

    private FileSystem fs;
    private Path root;

    @BeforeEach
    void setUp() {
        fs = Jimfs.newFileSystem(Configuration.unix());
        root = fs.getPath("/minhas");
    }

    @AfterEach
    void tearDown() throws Exception {
        fs.close();
    }

    @Test
    void writesVaultFiles() throws Exception {
        writer.write(graph, root);

        assertThat(root.resolve(".obsidian/app.json")).exists();
        assertThat(root.resolve(".obsidian/graph.json")).content().contains("tag:#skill", "colorGroups");
        assertThat(root.resolve(".obsidian/templates.json")).content().contains("\"_templates\"");
        assertThat(root.resolve("_templates/Nova skill.md")).content().contains("name: nome-da-skill", "## Processo");
        assertThat(root.resolve("_graph/00 - Índice.md")).content()
                .contains("type: index", "[[_graph/categorias/Dev Java|Dev: Java]]", "[[_graph/skills/docs|docs]]");
        assertThat(root.resolve("_graph/categorias/Dev Java.md")).content()
                .contains("type: category", "[[_graph/skills/revisao|revisao]]", "[[_graph/skills/gerar-testes|gerar-testes]]");
        assertThat(root.resolve("biblioteca.canvas")).exists();
    }

    @Test
    void skillNoteLinksEverything() throws Exception {
        writer.write(graph, root);

        assertThat(root.resolve("_graph/skills/revisao.md")).content().contains(
                "type: skill", "category: \"Dev: Java\"", "  - skill", "  - java", "  - code-review",
                "[[skills/revisao/SKILL|SKILL.md]]",
                "[[_graph/categorias/Dev Java|Dev: Java]]",
                "[[skills/revisao/references/checklist|references/checklist.md]]",
                "[[skills/revisao/scripts/run.sh|scripts/run.sh]]",
                "## Relacionadas", "## Menciona", "[[_graph/skills/gerar-testes|gerar-testes]]");
        assertThat(root.resolve("_graph/skills/docs.md")).content()
                .doesNotContain("category:", "## Arquivos", "## Relacionadas")
                .contains("[[_graph/00 - Índice|minhas]]");
    }

    @Test
    void canvasIsValidJsonCanvasAndDeterministic() throws Exception {
        writer.write(graph, root);
        byte[] first = Files.readAllBytes(root.resolve("biblioteca.canvas"));
        Path other = fs.getPath("/outra");
        writer.write(graph, other);

        assertThat(Files.readAllBytes(other.resolve("biblioteca.canvas"))).isEqualTo(first);

        JsonNode canvas = new JsonMapper().readTree(first);
        List<JsonNode> nodes = canvas.get("nodes").valueStream().toList();
        assertThat(nodes).filteredOn(n -> n.get("type").asString().equals("group"))
                .extracting(n -> n.get("label").asString()).containsExactly("Dev: Java", "Sem categoria");
        assertThat(nodes).filteredOn(n -> n.get("type").asString().equals("file"))
                .extracting(n -> n.get("file").asString())
                .containsExactly("skills/revisao/SKILL.md", "skills/gerar-testes/SKILL.md", "skills/docs/SKILL.md");
        assertThat(nodes).allSatisfy(n -> assertThat(n.get("id").asString()).matches("[0-9a-f]{16}"));
        List<String> ids = nodes.stream().map(n -> n.get("id").asString()).toList();
        assertThat(canvas.get("edges").valueStream().toList()).singleElement().satisfies(edge -> {
            assertThat(ids).contains(edge.get("fromNode").asString(), edge.get("toNode").asString());
            assertThat(edge.has("text")).isFalse();
        });
    }

    @Test
    void vaultNeverTouchesSkillFolders() throws Exception {
        writer.write(graph, root);

        try (Stream<Path> files = Files.walk(root)) {
            assertThat(files.filter(Files::isRegularFile).map(p -> root.relativize(p).toString()))
                    .noneMatch(p -> p.startsWith("skills/"));
        }
    }
}
