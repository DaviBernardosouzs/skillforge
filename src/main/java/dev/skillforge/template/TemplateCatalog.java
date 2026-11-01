package dev.skillforge.template;

import dev.skillforge.parsing.FrontmatterSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Carrega no startup os modelos de {@code templates/skills/*.md}. Cada arquivo tem um frontmatter
 * com {@code title} e {@code description} do modelo; o corpo usa os placeholders {{name}},
 * {{title}} e {{description}} da skill que será criada. O id é o nome do arquivo.
 */
@Component
public class TemplateCatalog {

    private static final String LOCATION = "classpath:templates/skills/*.md";

    private final List<SkillTemplate> templates;
    private final TemplateRenderer renderer;
    private final SkillMarkdownWriter markdownWriter;

    public TemplateCatalog(FrontmatterSplitter splitter, TemplateRenderer renderer, SkillMarkdownWriter markdownWriter) {
        this.renderer = renderer;
        this.markdownWriter = markdownWriter;
        this.templates = load(splitter);
    }

    public List<SkillTemplate> all() {
        return templates;
    }

    public Optional<SkillTemplate> find(String id) {
        return templates.stream().filter(t -> t.id().equals(id)).findFirst();
    }

    /** Monta o SKILL.md completo a partir de um modelo. */
    public String fill(String templateId, String name, String description) {
        SkillTemplate template = find(templateId).orElseThrow(() -> new TemplateNotFoundException(templateId));
        Map<String, String> context = Map.of(
                "name", name,
                "title", SkillTitles.fromName(name),
                "description", description);
        String body = renderer.renderString(template.content(), context);
        return markdownWriter.write(name, description, Map.of(), body);
    }

    private static List<SkillTemplate> load(FrontmatterSplitter splitter) {
        YAMLMapper yaml = new YAMLMapper();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(LOCATION);
            return Arrays.stream(resources)
                    .map(resource -> toTemplate(resource, splitter, yaml))
                    .sorted(Comparator.comparing(SkillTemplate::id))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler os modelos de skill", e);
        }
    }

    private static SkillTemplate toTemplate(Resource resource, FrontmatterSplitter splitter, YAMLMapper yaml) {
        try {
            String text = resource.getContentAsString(StandardCharsets.UTF_8);
            FrontmatterSplitter.Split split = splitter.split(text);
            Map<String, String> meta = yaml.readValue(
                    split.frontmatter().orElseThrow(), new TypeReference<Map<String, String>>() {
                    });
            String id = resource.getFilename().replaceFirst("\\.md$", "");
            return new SkillTemplate(id, meta.get("title"), meta.get("description"), split.body().strip());
        } catch (IOException e) {
            throw new UncheckedIOException("Modelo inválido: " + resource.getFilename(), e);
        }
    }
}
