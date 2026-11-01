package dev.skillforge.domain;

/** Lançada quando se tenta criar um {@link SkillName} fora do formato kebab-case ou do tamanho máximo. */
public class InvalidSkillNameException extends IllegalArgumentException {

    public InvalidSkillNameException(String value) {
        super("Nome inválido: '" + value + "'. Use kebab-case (ex.: minha-skill) com até "
                + SkillName.MAX_LENGTH + " caracteres.");
    }
}
