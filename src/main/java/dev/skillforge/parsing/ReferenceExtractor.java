package dev.skillforge.parsing;

import dev.skillforge.domain.RelativePaths;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Percorre o Markdown do corpo e coleta os destinos de links e imagens relativos,
 * ignorando URLs com esquema (http:, https:, mailto:...) e âncoras (#secao).
 */
@Component
public class ReferenceExtractor {

    /** Qualquer esquema de URI, ex.: "http:", "mailto:", "tel:". */
    private static final Pattern URI_SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:");

    private final Parser parser = Parser.builder().build();

    public Set<String> extract(String markdown) {
        Set<String> references = new LinkedHashSet<>();
        Node document = parser.parse(markdown == null ? "" : markdown);
        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Link link) {
                add(link.getDestination());
                visitChildren(link);
            }

            @Override
            public void visit(Image image) {
                add(image.getDestination());
                visitChildren(image);
            }

            private void add(String destination) {
                toRelativePath(destination).ifPresent(references::add);
            }
        });
        return references;
    }

    private static Optional<String> toRelativePath(String destination) {
        if (destination == null || destination.isBlank()
                || destination.startsWith("#") || URI_SCHEME.matcher(destination).find()) {
            return Optional.empty();
        }
        String path = stripSuffix(stripSuffix(destination, '#'), '?');
        String normalized = RelativePaths.normalize(decodePercent(path));
        return normalized.isEmpty() ? Optional.empty() : Optional.of(normalized);
    }

    /** Converte "%20" em espaço etc. Em Markdown "+" é literal, então não usamos a regra de formulário. */
    private static String decodePercent(String path) {
        if (!path.contains("%")) {
            return path;
        }
        try {
            return URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException malformed) {
            return path;
        }
    }

    private static String stripSuffix(String value, char marker) {
        int index = value.indexOf(marker);
        return index < 0 ? value : value.substring(0, index);
    }
}
