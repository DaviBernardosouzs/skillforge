package dev.skillforge.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    static final String REVIEW = """
            ---
            name: revisao-de-codigo
            description: Revisa PRs Java. Use quando o usuário pedir revisão de código.
            ---

            # Revisão de código

            Siga o [checklist](references/checklist.md). Depois chame gerar-testes.
            """;
    static final String TESTS = """
            ---
            name: gerar-testes
            description: Gera testes JUnit. Use quando o usuário pedir testes.
            ---

            # Gerar testes
            """;

    @Autowired
    MockMvc mvc;

    private final JsonMapper json = new JsonMapper();

    @Test
    void generateReturnsZipWithRepository() throws Exception {
        MvcResult result = mvc.perform(post("/api/libraries/generate").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validLibrary())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"minhas-skills.zip\""))
                .andExpect(header().string("X-Lint-Warnings", "0"))
                .andReturn();

        Map<String, String> zip = unzip(result.getResponse().getContentAsByteArray());
        assertThat(zip.keySet()).contains(
                "minhas-skills/skills/revisao-de-codigo/SKILL.md",
                "minhas-skills/skills/revisao-de-codigo/references/checklist.md",
                "minhas-skills/skills/gerar-testes/SKILL.md",
                "minhas-skills/.github/workflows/validate.yml",
                "minhas-skills/_graph/skills/revisao-de-codigo.md",
                "minhas-skills/.obsidian/graph.json",
                "minhas-skills/biblioteca.canvas");
        assertThat(zip.get("minhas-skills/.git/HEAD")).isEqualTo("ref: refs/heads/main\n");
        assertThat(zip.get("minhas-skills/skills/revisao-de-codigo/SKILL.md")).startsWith("---\nname: revisao-de-codigo\n");
        assertThat(zip.get("minhas-skills/LICENSE")).contains("MIT License");
    }

    @Test
    void generateCountsWarningsInHeader() throws Exception {
        Map<String, Object> library = validLibrary();
        skills(library).get(1).put("content", TESTS.replace(" Use quando o usuário pedir testes.", ""));

        mvc.perform(post("/api/libraries/generate").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(library)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Lint-Warnings", "1"));
    }

    @Test
    void invalidLibraryReturns422WithAllViolationsAtOnce() throws Exception {
        Map<String, Object> library = validLibrary();
        Map<String, Object> broken = skills(library).getFirst();
        broken.put("content", REVIEW.replace("name: revisao-de-codigo", "name: Revisao_De_Codigo")
                .replace("references/checklist.md", "references/nao-existe.md"));
        broken.put("files", List.of(Map.of("path", "../../etc/passwd", "content", "x")));

        mvc.perform(post("/api/libraries/generate").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(library)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.violations[*].ruleId",
                        containsInAnyOrder("name-format", "references-exist", "file-path")));
    }

    @Test
    void tooManySkillsReturns413() throws Exception {
        Map<String, Object> library = validLibrary();
        library.put("skills", Collections.nCopies(51, Map.of("content", TESTS)));

        mvc.perform(post("/api/libraries/generate").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(library)))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.detail", startsWith("A biblioteca tem 51 skills")));
    }

    @Test
    void fileAboveLimitReturns413() throws Exception {
        Map<String, Object> library = validLibrary();
        skills(library).getFirst().put("files", List.of(
                Map.of("path", "references/checklist.md", "content", "x".repeat(1_048_577))));

        mvc.perform(post("/api/libraries/lint").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(library)))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void invalidBodyReturns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/libraries/generate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Nome Ruim\", \"author\": \"\", \"license\": \"MIT\", \"skills\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("name", "author", "skills")));

        mvc.perform(post("/api/libraries/generate").contentType(MediaType.APPLICATION_JSON).content("{nao é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"));
    }

    @Test
    void base64FilesAreDecoded() throws Exception {
        Map<String, Object> library = validLibrary();
        skills(library).getFirst().put("files", List.of(
                Map.of("path", "references/checklist.md", "content", "LSBh", "encoding", "base64")));

        MvcResult result = mvc.perform(post("/api/libraries/generate").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(library)))
                .andExpect(status().isOk()).andReturn();

        assertThat(unzip(result.getResponse().getContentAsByteArray()))
                .containsEntry("minhas-skills/skills/revisao-de-codigo/references/checklist.md", "- a");
    }

    @Test
    void libraryLintReportsCrossSkillRules() throws Exception {
        Map<String, Object> library = validLibrary();
        skills(library).get(1).put("content", REVIEW);
        skills(library).get(1).put("related", List.of("fantasma"));

        mvc.perform(post("/api/libraries/lint").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(library)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasErrors").value(true))
                .andExpect(jsonPath("$.violations[*].ruleId", hasItem("name-unique")))
                .andExpect(jsonPath("$.violations[*].ruleId", hasItem("related-exists")));
    }

    @Test
    void graphPreviewReturnsNodesAndEdges() throws Exception {
        mvc.perform(post("/api/libraries/graph").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validLibrary())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes[0].type").value("INDEX"))
                .andExpect(jsonPath("$.nodes[*].id", hasItem("skill:gerar-testes")))
                .andExpect(jsonPath("$.edges[?(@.type == 'RELATED')]", hasSize(1)))
                .andExpect(jsonPath("$.edges[?(@.type == 'MENTIONS')]", hasSize(1)));
    }

    @Test
    void skillLintReturnsReport() throws Exception {
        mvc.perform(post("/api/skills/lint").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"# sem frontmatter\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCount").value(1))
                .andExpect(jsonPath("$.violations[0].ruleId").value("frontmatter-present"));
    }

    @Test
    void templatesAndFromTemplateWork() throws Exception {
        mvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder("code-review", "document-generator", "script-workflow")))
                .andExpect(jsonPath("$[0].content").doesNotExist());

        mvc.perform(post("/api/skills/from-template").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateId\": \"code-review\", \"name\": \"revisao\", \"description\": \"Revisa: PRs. Use quando pedirem.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", startsWith("---\nname: revisao\n")));

        mvc.perform(post("/api/skills/from-template").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateId\": \"nao-existe\", \"name\": \"a\", \"description\": \"b\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Modelo não encontrado"));
    }

    @Test
    void draftEndpointsWork() throws Exception {
        mvc.perform(get("/api/skills/draft/template"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("name"))
                .andExpect(jsonPath("$[?(@.required == true)].name",
                        containsInAnyOrder("name", "purpose", "triggers", "steps")));

        mvc.perform(post("/api/skills/draft").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "revisao", "purpose": "Revisa PRs", "triggers": ["pedirem revisão"],
                         "steps": [{"title": "Leia", "detail": "Leia o diff inteiro antes de comentar."}],
                         "resources": [{"kind": "REFERENCE", "fileName": "checklist", "purpose": "Critérios"}]}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillMd", startsWith("---\nname: revisao\n")))
                .andExpect(jsonPath("$.stubFiles[0].path").value("references/checklist.md"))
                .andExpect(jsonPath("$.lint.errorCount").value(0))
                .andExpect(jsonPath("$.hints[*].id", hasItem("few-triggers")));

        mvc.perform(post("/api/skills/draft").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actuatorExposesOnlyHealthAndInfo() throws Exception {
        mvc.perform(get("/actuator/info")).andExpect(status().isOk());
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/libraries/generate']").exists());
    }

    // ---- helpers ----

    static Map<String, Object> validLibrary() {
        Map<String, Object> review = new HashMap<>(Map.of(
                "content", REVIEW,
                "files", List.of(Map.of("path", "references/checklist.md", "content", "- Nomes claros")),
                "category", "desenvolvimento",
                "tags", List.of("java"),
                "related", List.of("gerar-testes")));
        Map<String, Object> tests = new HashMap<>(Map.of("content", TESTS, "category", "desenvolvimento"));
        return new HashMap<>(Map.of("name", "minhas-skills", "author", "Bernardo", "license", "MIT",
                "skills", new ArrayList<>(List.of(review, tests))));
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> skills(Map<String, Object> library) {
        return (List<Map<String, Object>>) library.get("skills");
    }

    static Map<String, String> unzip(byte[] bytes) throws Exception {
        Map<String, String> entries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                entries.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return entries;
    }
}
