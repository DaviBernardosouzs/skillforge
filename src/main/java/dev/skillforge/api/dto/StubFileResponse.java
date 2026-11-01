package dev.skillforge.api.dto;

import dev.skillforge.domain.SkillFile;

import java.nio.charset.StandardCharsets;

/** Arquivo-esqueleto do assistente, no mesmo formato de FileRequest para voltar direto à biblioteca. */
public record StubFileResponse(String path, String content, String encoding) {

    public static StubFileResponse from(SkillFile file) {
        return new StubFileResponse(file.relativePath(), new String(file.content(), StandardCharsets.UTF_8), "utf8");
    }
}
