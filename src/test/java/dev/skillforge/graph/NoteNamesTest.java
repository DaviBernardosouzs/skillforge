package dev.skillforge.graph;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class NoteNamesTest {

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "desenvolvimento;desenvolvimento",
            "Dev / Ops;Dev Ops",
            "a\\b:c*d?e\"f<g>h|i;abcdefghi",
            "#tag ^bloco [x];tag bloco x",
            "fim.;fim",
            "???;sem-nome",
            "Produtividade & Docs;Produtividade & Docs",
            "Documentação técnica;Documentacao tecnica",
            "日本;sem-nome"
    })
    void sanitizesNoteNames(String input, String expected) {
        assertThat(NoteNames.sanitize(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "java;java",
            "#review;review",
            "code review;code-review",
            "2026;_2026",
            "c++;c",
            "nested/tag;nested/tag"
    })
    void sanitizesTags(String input, String expected) {
        assertThat(NoteNames.tag(input)).isEqualTo(expected);
    }
}
