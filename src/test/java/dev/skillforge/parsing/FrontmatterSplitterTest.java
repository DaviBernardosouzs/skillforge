package dev.skillforge.parsing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrontmatterSplitterTest {

    private final FrontmatterSplitter splitter = new FrontmatterSplitter();

    @Test
    void splitsFrontmatterAndBody() {
        var split = splitter.split("---\nname: a\n---\n# Corpo\n");

        assertThat(split.frontmatter()).contains("name: a");
        assertThat(split.body()).isEqualTo("# Corpo\n");
    }

    @Test
    void acceptsWindowsLineEndings() {
        var split = splitter.split("---\r\nname: a\r\ndescription: b\r\n---\r\ncorpo");

        assertThat(split.frontmatter()).contains("name: a\ndescription: b");
        assertThat(split.body()).isEqualTo("corpo");
    }

    @Test
    void missingFrontmatterKeepsWholeTextAsBody() {
        var split = splitter.split("# Só corpo");

        assertThat(split.frontmatter()).isEmpty();
        assertThat(split.body()).isEqualTo("# Só corpo");
    }

    @Test
    void unclosedBlockIsTreatedAsMissing() {
        assertThat(splitter.split("---\nname: a\n# sem fechar").frontmatter()).isEmpty();
    }

    @Test
    void delimiterMustBeOnFirstLine() {
        assertThat(splitter.split("\n---\nname: a\n---\n").frontmatter()).isEmpty();
    }

    @Test
    void emptyBodyIsAllowed() {
        var split = splitter.split("---\nname: a\n---");

        assertThat(split.frontmatter()).contains("name: a");
        assertThat(split.body()).isEmpty();
    }
}
