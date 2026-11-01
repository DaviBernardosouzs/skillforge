package dev.skillforge.generation;

import java.io.IOException;
import java.nio.file.Path;

/** Transforma a pasta num repositório Git com um commit inicial na branch main. */
public interface GitInitializer {

    String INITIAL_COMMIT_MESSAGE = "chore: biblioteca gerada pelo SkillForge";
    String BRANCH = "main";

    void init(Path root, String author) throws IOException;
}
