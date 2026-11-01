package dev.skillforge.parsing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceExtractorTest {

    private final ReferenceExtractor extractor = new ReferenceExtractor();

    @Test
    void collectsRelativeLinksAndImages() {
        String body = """
                Siga o [checklist](references/checklist.md) e veja ![diagrama](assets/fluxo.png).
                Rode [o script](./scripts/run.sh).
                """;

        assertThat(extractor.extract(body))
                .containsExactly("references/checklist.md", "assets/fluxo.png", "scripts/run.sh");
    }

    @Test
    void ignoresAbsoluteUrlsMailtoAndAnchors() {
        String body = "[a](https://x.dev) [b](http://x.dev) [c](mailto:a@b.dev) [d](#secao) <https://auto.link>";

        assertThat(extractor.extract(body)).isEmpty();
    }

    @Test
    void stripsFragmentQueryAndDecodesSpaces() {
        String body = "[a](references/guia.md#parte-2) [b](references/meu%20arquivo.md?v=1)";

        assertThat(extractor.extract(body)).containsExactly("references/guia.md", "references/meu arquivo.md");
    }

    @Test
    void normalizesDotSegmentsButKeepsEscapes() {
        assertThat(extractor.extract("[a](references/x/../a.md) [b](../fora.md)"))
                .containsExactly("references/a.md", "../fora.md");
    }

    @Test
    void ignoresLinksInsideCodeBlocks() {
        assertThat(extractor.extract("```\n[a](references/nao.md)\n```\n`[b](references/nao2.md)`")).isEmpty();
    }

    @Test
    void returnsDistinctValues() {
        assertThat(extractor.extract("[a](references/a.md) [b](references/a.md)")).hasSize(1);
    }
}
