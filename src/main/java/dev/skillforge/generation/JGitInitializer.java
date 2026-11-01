package dev.skillforge.generation;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.StoredConfig;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

/** Cria o repositório com JGit (sem depender do git instalado na máquina). */
@Component
public class JGitInitializer implements GitInitializer {

    /** E-mail neutro: a pessoa ajusta com git config se quiser. */
    static final String AUTHOR_EMAIL = "skillforge@users.noreply.github.com";

    @Override
    public void init(Path root, String author) throws IOException {
        try (Git git = Git.init().setDirectory(root.toFile()).setInitialBranch(BRANCH).call()) {
            git.add().addFilepattern(".").call();
            PersonIdent ident = new PersonIdent(author, AUTHOR_EMAIL);
            git.commit()
                    .setMessage(INITIAL_COMMIT_MESSAGE)
                    .setAuthor(ident)
                    .setCommitter(ident)
                    .setSign(false) // ignora commit.gpgsign da máquina do servidor
                    .call();

            // O commit guarda validate.sh como executável (100755), mas o zip não preserva a
            // permissão. Sem isso, o git de quem descompacta veria uma mudança de modo e o
            // working tree não estaria limpo.
            StoredConfig config = git.getRepository().getConfig();
            config.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE, false);
            config.save();
        } catch (GitAPIException e) {
            throw new IOException("Falha ao criar o repositório Git", e);
        }
    }
}
