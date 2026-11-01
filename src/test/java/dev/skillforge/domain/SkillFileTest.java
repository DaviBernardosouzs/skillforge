package dev.skillforge.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkillFileTest {

    @Test
    void equalsComparesContentNotArrayReference() {
        SkillFile a = new SkillFile("references/a.md", new byte[]{1, 2, 3});
        SkillFile b = new SkillFile("references/a.md", new byte[]{1, 2, 3});

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(new SkillFile("references/a.md", new byte[]{9}));
    }

    @Test
    void isImmutable() {
        byte[] original = {1, 2, 3};
        SkillFile file = new SkillFile("assets/x.bin", original);

        original[0] = 42;
        file.content()[1] = 42;

        assertThat(file.content()).containsExactly(1, 2, 3);
    }

    @Test
    void toStringShowsSizeInsteadOfBytes() {
        assertThat(SkillFile.ofText("references/a.md", "abc").toString())
                .isEqualTo("SkillFile[relativePath=references/a.md, bytes=3]");
    }
}
