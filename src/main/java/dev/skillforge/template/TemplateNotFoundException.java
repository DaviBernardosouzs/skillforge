package dev.skillforge.template;

/** Pedido de um modelo de skill que não existe no catálogo (vira 404 na API). */
public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String id) {
        super("Modelo de skill '" + id + "' não existe.");
    }
}
