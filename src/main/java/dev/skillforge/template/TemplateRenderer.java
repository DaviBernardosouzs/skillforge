package dev.skillforge.template;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renderiza os templates Mustache de {@code src/main/resources/templates/}.
 * O escape de HTML fica desligado porque geramos Markdown, YAML e shell, não páginas web.
 */
@Component
public class TemplateRenderer {

    private static final String ROOT = "templates/";
    private static final String SUFFIX = ".mustache";

    private final Mustache.Compiler compiler = Mustache.compiler()
            .escapeHTML(false)
            .nullValue("")
            .emptyStringIsFalse(true);

    private final Map<String, Template> cache = new ConcurrentHashMap<>();

    /**
     * @param name caminho sem extensão, relativo a templates/ (ex.: "repo/README.md")
     */
    public String render(String name, Map<String, ?> context) {
        return cache.computeIfAbsent(name, this::compile).execute(context);
    }

    /** Renderiza um template que veio como texto (ex.: os modelos de skill do catálogo). */
    public String renderString(String template, Map<String, ?> context) {
        return compiler.compile(template).execute(context);
    }

    private Template compile(String name) {
        return compiler.compile(read(ROOT + name + SUFFIX));
    }

    static String read(String classpathLocation) {
        try (var in = new ClassPathResource(classpathLocation).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Template não encontrado: " + classpathLocation, e);
        }
    }
}
