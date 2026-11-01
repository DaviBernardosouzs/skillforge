package dev.skillforge.generation;

import dev.skillforge.domain.License;

import java.util.List;

/**
 * Entrada do gerador, independente do formato HTTP. A API converte o LibraryRequest neste tipo,
 * assim o pacote generation não depende do pacote api.
 */
public record LibraryInput(String name, String author, License license, List<SkillInput> skills) {

    public LibraryInput {
        skills = skills == null ? List.of() : List.copyOf(skills);
    }
}
