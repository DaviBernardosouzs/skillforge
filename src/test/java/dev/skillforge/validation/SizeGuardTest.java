package dev.skillforge.validation;

import dev.skillforge.config.SkillLimits;
import dev.skillforge.domain.SkillFile;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SizeGuardTest {

    private final SizeGuard guard = new SizeGuard(new SkillLimits(64, 1024, 500, 2, 10, 25));

    @Test
    void acceptsWithinLimits() {
        assertThatCode(() -> guard.check(List.of("12345"), List.of(List.of(new SkillFile("references/a", new byte[10])))))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsTooManySkills() {
        assertThatThrownBy(() -> guard.check(List.of("a", "b", "c"), Collections.nCopies(3, List.of())))
                .isInstanceOf(LimitExceededException.class).hasMessageContaining("máximo é 2");
    }

    @Test
    void rejectsFileAboveLimit() {
        assertThatThrownBy(() -> guard.check(List.of("a"), List.of(List.of(new SkillFile("assets/big", new byte[11])))))
                .isInstanceOf(LimitExceededException.class).hasMessageContaining("assets/big");
    }

    @Test
    void rejectsLibraryAboveTotalLimit() {
        var files = List.of(new SkillFile("assets/a", new byte[10]), new SkillFile("assets/b", new byte[10]));
        assertThatThrownBy(() -> guard.check(List.of("123456"), List.of(files)))
                .isInstanceOf(LimitExceededException.class).hasMessageContaining("26 bytes");
    }
}
