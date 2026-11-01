package dev.skillforge.validation;

import dev.skillforge.config.SkillLimits;
import dev.skillforge.domain.SkillFile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Confere quantidade de skills e tamanho dos arquivos antes de qualquer trabalho pesado. */
@Component
public class SizeGuard {

    private final SkillLimits limits;

    public SizeGuard(SkillLimits limits) {
        this.limits = limits;
    }

    /**
     * @param contents conteúdo bruto de cada SKILL.md enviado
     * @param files    arquivos de apoio de cada skill, na mesma ordem
     */
    public void check(List<String> contents, List<List<SkillFile>> files) {
        if (contents.size() > limits.maxSkillsPerLibrary()) {
            throw new LimitExceededException("A biblioteca tem " + contents.size()
                    + " skills; o máximo é " + limits.maxSkillsPerLibrary() + ".");
        }
        long total = 0;
        for (String content : contents) {
            long size = content == null ? 0 : content.getBytes(StandardCharsets.UTF_8).length;
            checkFile("SKILL.md", size);
            total += size;
        }
        for (List<SkillFile> skillFiles : files) {
            for (SkillFile file : skillFiles) {
                checkFile(file.relativePath(), file.size());
                total += file.size();
            }
        }
        if (total > limits.maxLibraryBytes()) {
            throw new LimitExceededException("A biblioteca tem " + total
                    + " bytes; o máximo é " + limits.maxLibraryBytes() + " bytes.");
        }
    }

    private void checkFile(String name, long size) {
        if (size > limits.maxFileBytes()) {
            throw new LimitExceededException("O arquivo '" + name + "' tem " + size
                    + " bytes; o máximo por arquivo é " + limits.maxFileBytes() + " bytes.");
        }
    }
}
