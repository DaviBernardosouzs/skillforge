package dev.skillforge.generation;

import dev.skillforge.domain.Library;
import dev.skillforge.domain.License;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.StreamSupport;

import static dev.skillforge.domain.Skills.skill;
import static org.assertj.core.api.Assertions.assertThat;

class JGitInitializerTest {

    @TempDir
    Path temp;

    @Test
    void createsSingleCommitOnMainWithCleanTree() throws Exception {
        Path root = temp.resolve("minhas-skills");
        Files.createDirectories(root);
        Generators.repositoryWriter().write(new Library("minhas-skills", "Bernardo", License.MIT, List.of(skill("a"))), root);

        new JGitInitializer().init(root, "Bernardo");

        try (Git git = Git.open(root.toFile())) {
            List<RevCommit> commits = StreamSupport.stream(git.log().call().spliterator(), false).toList();
            assertThat(commits).singleElement().satisfies(commit -> {
                assertThat(commit.getFullMessage()).isEqualTo("chore: biblioteca gerada pelo SkillForge");
                assertThat(commit.getAuthorIdent().getName()).isEqualTo("Bernardo");
            });
            assertThat(git.getRepository().getBranch()).isEqualTo("main");

            Status status = git.status().call();
            assertThat(status.isClean()).isTrue();

            try (TreeWalk walk = TreeWalk.forPath(git.getRepository(), "scripts/validate.sh", commits.getFirst().getTree())) {
                assertThat(walk.getFileMode(0)).isEqualTo(FileMode.EXECUTABLE_FILE);
            }
        }
        assertThat(root.resolve(".git/HEAD")).content().contains("refs/heads/main");
    }
}
