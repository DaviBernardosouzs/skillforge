package dev.skillforge.generation;

import dev.skillforge.domain.License;
import dev.skillforge.domain.SkillFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Contrato entre o linter Java e o validate.sh gerado: o script passa no repositório gerado
 * e falha quando alguém quebra um SKILL.md depois.
 */
@EnabledOnOs({OS.LINUX, OS.MAC})
class ValidateScriptContractTest {

    @TempDir
    Path temp;

    private Path repo;

    @BeforeEach
    void generateAndUnzip() throws Exception {
        String longDescription = "Revisa PRs: código \"Java\" e testes. Use quando o usuário pedir revisão. " + "x".repeat(800);
        LibraryInput input = new LibraryInput("minhas-skills", "Bernardo", License.MIT, List.of(
                new SkillInput("---\nname: revisao\ndescription: " + "'" + longDescription + "'" + "\n---\n# R\n[c](references/c.md)",
                        List.of(SkillFile.ofText("references/c.md", "x")), null),
                new SkillInput("---\nname: docs\ndescription: Gera docs. Use quando pedirem docs.\n---\n# D", List.of(), null)));
        byte[] zip = Generators.generator().generate(input).zip();
        unzip(zip, temp);
        repo = temp.resolve("minhas-skills");
    }

    @Test
    void passesOnGeneratedRepository() throws Exception {
        Result result = runValidate();

        assertThat(result.exitCode()).as(result.output()).isZero();
        assertThat(result.output()).contains("2 skill(s) válida(s)");
    }

    @Test
    void failsOnBrokenSkills() throws Exception {
        Files.writeString(repo.resolve("skills/docs/SKILL.md"), "---\nname: Docs_Errado\n---\n# sem description\n");
        Files.createDirectories(repo.resolve("skills/sem-frontmatter"));
        Files.writeString(repo.resolve("skills/sem-frontmatter/SKILL.md"), "# nada\n");
        Files.writeString(repo.resolve("skills/revisao/SKILL.md"),
                "---\nname: outro-nome\ndescription: " + "y".repeat(1025) + "\n---\n");

        Result result = runValidate();

        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.output()).contains(
                "skills/docs/SKILL.md: name 'Docs_Errado' precisa estar em kebab-case",
                "skills/docs/SKILL.md: falta o campo 'description'",
                "skills/sem-frontmatter/SKILL.md: o arquivo precisa começar",
                "skills/revisao/SKILL.md: name 'outro-nome' deve ser igual ao nome da pasta 'revisao'",
                "skills/revisao/SKILL.md: description tem 1025 caracteres");
    }

    @Test
    void ignoresObsidianFolders() throws Exception {
        assertThat(repo.resolve("_templates/Nova skill.md")).exists();

        assertThat(runValidate().exitCode()).isZero();
    }

    @Test
    void acceptsMultilineBlockDescription() throws Exception {
        Files.writeString(repo.resolve("skills/docs/SKILL.md"),
                "---\nname: docs\ndescription: >\n  Gera docs.\n  Use quando pedirem.\nlicense: MIT\n---\n# D\n");

        assertThat(runValidate().exitCode()).isZero();
    }

    @Test
    void unzippedRepositoryHasOneCommitAndCleanTreeForRealGit() throws Exception {
        assumeTrue(commandExists("git"), "git não instalado");

        Result log = run("git", "log", "--oneline");
        Result status = run("git", "status", "--porcelain");
        Result branch = run("git", "branch", "--show-current");

        assertThat(log.output().lines()).singleElement().asString().endsWith("chore: biblioteca gerada pelo SkillForge");
        assertThat(status.output()).isEmpty();
        assertThat(branch.output().strip()).isEqualTo("main");
    }

    private static boolean commandExists(String command) {
        try {
            return new ProcessBuilder(command, "--version").start().waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private record Result(int exitCode, String output) {
    }

    private Result runValidate() throws IOException, InterruptedException {
        return run("bash", "scripts/validate.sh");
    }

    private Result run(String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .directory(repo.toFile())
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertThat(process.waitFor(30, TimeUnit.SECONDS)).isTrue();
        return new Result(process.exitValue(), output);
    }

    static void unzip(byte[] zip, Path target) throws IOException {
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry entry; (entry = in.getNextEntry()) != null; ) {
                Path out = target.resolve(entry.getName()).normalize();
                if (!out.startsWith(target)) {
                    throw new IOException("Entrada fora da pasta: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.createDirectories(out.getParent());
                    Files.write(out, in.readAllBytes());
                }
            }
        }
    }
}
