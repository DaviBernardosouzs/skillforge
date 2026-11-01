package dev.skillforge.parsing;

import dev.skillforge.domain.RawSkill;

/**
 * Resultado da leitura de um SKILL.md. Em vez de lançar exceção, o parser devolve
 * {@link Failure} e o linter transforma isso na violação {@code frontmatter-present}.
 */
public sealed interface ParseResult {

    record Success(RawSkill skill) implements ParseResult {
    }

    record Failure(String message) implements ParseResult {
    }
}
