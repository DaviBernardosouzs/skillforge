package dev.skillforge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadados do Swagger em /swagger-ui.html. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI skillforgeOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SkillForge API")
                .version("0.0.1")
                .description("Gera bibliotecas de Agent Skills como repositórios Git prontos para o GitHub, "
                        + "com validação, assistente de redação e vault do Obsidian."));
    }
}
