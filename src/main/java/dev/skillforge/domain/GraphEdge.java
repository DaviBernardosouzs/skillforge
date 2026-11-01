package dev.skillforge.domain;

/** Aresta dirigida do grafo. */
public record GraphEdge(String from, String to, Type type) {

    public enum Type {
        /** índice → categoria, categoria → skill, índice → skill sem categoria */
        CONTAINS,
        /** skill → arquivo de references/ ou scripts/ */
        HAS_FILE,
        /** skill → skill listada em related */
        RELATED,
        /** skill → skill cujo name aparece no corpo */
        MENTIONS
    }
}
