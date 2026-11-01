package dev.skillforge.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class RelativePathsTest {

    @ParameterizedTest
    @CsvSource({
            "references/a.md, references/a.md",
            "./references/a.md, references/a.md",
            "references//a.md, references/a.md",
            "references/x/../a.md, references/a.md",
            "references/../../x, ../x",
            "../a, ../a",
            "/etc/passwd, /etc/passwd",
            "references\\a.md, references/a.md"
    })
    void normalizes(String input, String expected) {
        assertThat(RelativePaths.normalize(input)).isEqualTo(expected);
    }
}
