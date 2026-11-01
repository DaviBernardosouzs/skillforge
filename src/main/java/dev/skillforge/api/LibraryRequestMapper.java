package dev.skillforge.api;

import dev.skillforge.api.dto.FileRequest;
import dev.skillforge.api.dto.LibraryRequest;
import dev.skillforge.api.dto.SkillRequest;
import dev.skillforge.domain.SkillFile;
import dev.skillforge.domain.SkillGraphInfo;
import dev.skillforge.generation.LibraryInput;
import dev.skillforge.generation.SkillInput;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/** Converte os DTOs HTTP nos tipos de entrada do gerador, decodificando os arquivos. */
final class LibraryRequestMapper {

    private LibraryRequestMapper() {
    }

    static LibraryInput toInput(LibraryRequest request) {
        return new LibraryInput(request.name(), request.author().strip(), request.license(),
                request.skills().stream().map(LibraryRequestMapper::toInput).toList());
    }

    static SkillInput toInput(SkillRequest request) {
        return new SkillInput(request.content(), toFiles(request.files()),
                new SkillGraphInfo(request.category(), request.tags(), request.related()));
    }

    static List<SkillFile> toFiles(List<FileRequest> files) {
        return files == null ? List.of() : files.stream().map(LibraryRequestMapper::toFile).toList();
    }

    /** @throws IllegalArgumentException se o base64 for inválido (vira 400) */
    private static SkillFile toFile(FileRequest file) {
        byte[] content = file.isBase64()
                ? decodeBase64(file)
                : file.content().getBytes(StandardCharsets.UTF_8);
        return new SkillFile(file.path(), content);
    }

    private static byte[] decodeBase64(FileRequest file) {
        try {
            return Base64.getDecoder().decode(file.content().replaceAll("\\s", ""));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("O arquivo '" + file.path() + "' não está em base64 válido.", e);
        }
    }
}
