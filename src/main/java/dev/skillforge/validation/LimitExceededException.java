package dev.skillforge.validation;

/** Requisição maior que os limites configurados (vira 413 Payload Too Large na API). */
public class LimitExceededException extends RuntimeException {

    public LimitExceededException(String message) {
        super(message);
    }
}
