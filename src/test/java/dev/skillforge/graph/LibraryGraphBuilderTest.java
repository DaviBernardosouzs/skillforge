package dev.skillforge.graph;

import dev.skillforge.domain.GraphEdge;
import dev.skillforge.domain.GraphNode;
import dev.skillforge.domain.LibraryGraph;
import dev.skillforge.domain.Skill;
import dev.skillforge.domain.SkillFile;
import dev.skillforge.domain.SkillGraphInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.skillforge.domain.GraphEdge.Type.CONTAINS;
import static dev.skillforge.domain.GraphEdge.Type.HAS_FILE;
import static dev.skillforge.domain.GraphEdge.Type.MENTIONS;
import static dev.skillforge.domain.GraphEdge.Type.RELATED;
import static dev.skillforge.domain.Skills.graph;
import static dev.skillforge.domain.Skills.skill;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class LibraryGraphBuilderTest {

    private final LibraryGraphBuilder builder = new LibraryGraphBuilder();

    private final Skill review = skill("revisao", "Depois rode gerar-testes.",
            graph("dev", List.of("java"), List.of("gerar-testes")),
            SkillFile.ofText("references/checklist.md", "x"), SkillFile.ofText("scripts/run.sh", "x"),
            SkillFile.ofText("assets/logo.png", "x"));
    private final Skill tests = skill("gerar-testes", "Sem menções.", graph("dev", List.of(), List.of("nao-existe")));
    private final Skill docs = skill("docs", "Use a revisao-detalhada (outra coisa).", SkillGraphInfo.empty());

    @Test
    void createsNodesForIndexCategoriesSkillsAndFiles() {
        LibraryGraph graph = builder.build("minhas", List.of(review, tests, docs));

        assertThat(graph.nodes()).extracting(GraphNode::id, GraphNode::type).containsExactly(
                tuple("index", GraphNode.Type.INDEX),
                tuple("category:dev", GraphNode.Type.CATEGORY),
                tuple("skill:revisao", GraphNode.Type.SKILL),
                tuple("file:revisao/references/checklist.md", GraphNode.Type.REFERENCE),
                tuple("file:revisao/scripts/run.sh", GraphNode.Type.SCRIPT),
                tuple("skill:gerar-testes", GraphNode.Type.SKILL),
                tuple("skill:docs", GraphNode.Type.SKILL));
    }

    @Test
    void createsContainmentFileRelatedAndMentionEdges() {
        LibraryGraph graph = builder.build("minhas", List.of(review, tests, docs));

        assertThat(graph.edges()).containsExactlyInAnyOrder(
                new GraphEdge("index", "category:dev", CONTAINS),
                new GraphEdge("category:dev", "skill:revisao", CONTAINS),
                new GraphEdge("skill:revisao", "file:revisao/references/checklist.md", HAS_FILE),
                new GraphEdge("skill:revisao", "file:revisao/scripts/run.sh", HAS_FILE),
                new GraphEdge("skill:revisao", "skill:gerar-testes", RELATED),
                new GraphEdge("skill:revisao", "skill:gerar-testes", MENTIONS),
                new GraphEdge("category:dev", "skill:gerar-testes", CONTAINS),
                new GraphEdge("index", "skill:docs", CONTAINS));
    }

    @Test
    void relatedToMissingSkillIsIgnoredInGraph() {
        LibraryGraph graph = builder.build("minhas", List.of(tests));

        assertThat(graph.edges()).noneMatch(e -> e.to().equals("skill:nao-existe"));
    }

    @Test
    void mentionNeedsWholeKebabName() {
        assertThat(LibraryGraphBuilder.mentions("use revisao agora", "revisao")).isTrue();
        assertThat(LibraryGraphBuilder.mentions("use revisao-detalhada", "revisao")).isFalse();
        assertThat(LibraryGraphBuilder.mentions("pre-revisao", "revisao")).isFalse();
    }

    @Test
    void isDeterministic() {
        assertThat(builder.build("a", List.of(review, tests, docs))).isEqualTo(builder.build("a", List.of(review, tests, docs)));
    }
}
