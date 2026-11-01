package dev.skillforge.domain;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkillFactoryTest {

    @Test
    void convertsRawSkillPreservingExtrasOrder() {
        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("license", "MIT");
        extras.put("allowed-tools", "Bash");
        extras.put("compatibility", "Java 21");
        RawSkill raw = new RawSkill("minha-skill", "  Faz algo. Use quando precisar.  ", "# Corpo",
                extras, List.of(SkillFile.ofText("references/a.md", "a")));

        Skill skill = SkillFactory.fromRaw(raw);

        assertThat(skill.name()).isEqualTo("minha-skill");
        assertThat(skill.metadata().description()).isEqualTo("Faz algo. Use quando precisar.");
        assertThat(skill.metadata().extras().keySet()).containsExactly("license", "allowed-tools", "compatibility");
        assertThat(skill.files()).hasSize(1);
    }

    @Test
    void invalidNameStillFailsFast() {
        RawSkill raw = new RawSkill("Nome Ruim", "d", "", Map.of(), List.of());

        assertThatThrownBy(() -> SkillFactory.fromRaw(raw)).isInstanceOf(InvalidSkillNameException.class);
    }
}
