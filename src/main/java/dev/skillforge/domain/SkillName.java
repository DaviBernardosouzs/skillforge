package dev.skillforge.domain;

import java.util.regex.Pattern;

/**
 * Nome de skill (ou de biblioteca) em kebab-case. Só existe instância válida.
 */
public record SkillName(String value) {

    /** Limite da especificação oficial de Agent Skills. */
    public static final int MAX_LENGTH = 64;

    private static final Pattern KEBAB_CASE = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    public SkillName {
        if (!isValid(value, MAX_LENGTH)) {
            throw new InvalidSkillNameException(value);
        }
    }

    /** Verifica formato kebab-case e tamanho, sem lançar exceção. */
    public static boolean isValid(String value, int maxLength) {
        return isKebabCase(value) && value.length() <= maxLength;
    }

    public static boolean isKebabCase(String value) {
        return value != null && KEBAB_CASE.matcher(value).matches();
    }

    @Override
    public String toString() {
        return value;
    }
}
