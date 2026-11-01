package dev.skillforge.domain;

import java.util.List;
import java.util.Objects;

/** Biblioteca validada: vira a pasta raiz do repositório gerado. */
public record Library(String name, String author, License license, List<Skill> skills) {

    public Library {
        if (!SkillName.isKebabCase(name)) {
            throw new InvalidSkillNameException(name);
        }
        Objects.requireNonNull(author, "author");
        license = license == null ? License.NONE : license;
        skills = skills == null ? List.of() : List.copyOf(skills);
    }
}
