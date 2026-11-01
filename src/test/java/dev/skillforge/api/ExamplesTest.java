package dev.skillforge.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Os arquivos de examples/ citados no README precisam continuar funcionando. */
@SpringBootTest
@AutoConfigureMockMvc
class ExamplesTest {

    @Autowired
    MockMvc mvc;

    @Test
    void libraryExampleGeneratesWithoutWarnings() throws Exception {
        mvc.perform(post("/api/libraries/generate").contentType(MediaType.APPLICATION_JSON)
                        .content(Files.readString(Path.of("examples/library-request.json"))))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Lint-Warnings", "0"));
    }

    @Test
    void draftExampleProducesCleanSkillWithAllSections() throws Exception {
        mvc.perform(post("/api/skills/draft").contentType(MediaType.APPLICATION_JSON)
                        .content(Files.readString(Path.of("examples/skill-draft.json"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lint.violations", hasSize(0)))
                .andExpect(jsonPath("$.hints", hasSize(0)))
                .andExpect(jsonPath("$.stubFiles", hasSize(2)))
                .andExpect(jsonPath("$.skillMd", containsString("## Verificação antes de entregar")))
                .andExpect(jsonPath("$.skillMd", containsString("[exportar.sh](scripts/exportar.sh)")));
    }
}
