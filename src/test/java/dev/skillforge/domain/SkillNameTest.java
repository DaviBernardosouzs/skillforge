package dev.skillforge.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkillNameTest {

    @ParameterizedTest
    @ValueSource(strings = {"a", "revisao-de-codigo", "skill2", "a1-b2-c3"})
    void acceptsKebabCase(String value) {
        assertThat(new SkillName(value).value()).isEqualTo(value);
    }

    @Test
    void acceptsExactlyMaxLength() {
        String value = "a".repeat(64);
        assertThat(new SkillName(value).value()).hasSize(64);
    }

    @Test
    void rejectsOneCharAboveMaxLength() {
        assertThatThrownBy(() -> new SkillName("a".repeat(65)))
                .isInstanceOf(InvalidSkillNameException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "Revisao", "minha--skill", "-inicio", "fim-", "com espaco", "under_score", "acentuação"})
    void rejectsInvalidNames(String value) {
        assertThatThrownBy(() -> new SkillName(value))
                .isInstanceOf(InvalidSkillNameException.class)
                .hasMessageContaining("kebab-case");
    }

    @Test
    void isValidRespectsCustomMaxLength() {
        assertThat(SkillName.isValid("abcde", 5)).isTrue();
        assertThat(SkillName.isValid("abcdef", 5)).isFalse();
    }
}
