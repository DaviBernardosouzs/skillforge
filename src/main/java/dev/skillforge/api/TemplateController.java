package dev.skillforge.api;

import dev.skillforge.api.dto.ContentResponse;
import dev.skillforge.api.dto.FromTemplateRequest;
import dev.skillforge.api.dto.TemplateSummary;
import dev.skillforge.template.TemplateCatalog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Modelos prontos", description = "Modelos de SKILL.md para começar uma skill")
public class TemplateController {

    private final TemplateCatalog catalog;

    public TemplateController(TemplateCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/templates")
    @Operation(summary = "Lista os modelos de skill disponíveis")
    public List<TemplateSummary> list() {
        return catalog.all().stream().map(TemplateSummary::from).toList();
    }

    @PostMapping("/skills/from-template")
    @Operation(summary = "Gera um SKILL.md a partir de um modelo",
            description = "Preenche o modelo com o name e a description informados.")
    @ApiResponse(responseCode = "200", description = "SKILL.md preenchido")
    @ApiResponse(responseCode = "404", description = "Modelo não existe")
    public ContentResponse fromTemplate(@Valid @RequestBody FromTemplateRequest request) {
        return new ContentResponse(catalog.fill(request.templateId(), request.name().strip(), request.description().strip()));
    }
}
