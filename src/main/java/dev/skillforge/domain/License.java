package dev.skillforge.domain;

/** Licenças oferecidas para o repositório gerado. */
public enum License {
    MIT("MIT"),
    APACHE_2_0("Apache License 2.0"),
    CC_BY_4_0("CC BY 4.0"),
    NONE("Sem licença");

    private final String displayName;

    License(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
