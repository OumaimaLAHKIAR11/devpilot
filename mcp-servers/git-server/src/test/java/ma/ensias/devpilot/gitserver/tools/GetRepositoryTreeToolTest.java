
package ma.ensias.devpilot.gitserver.tools;

import ma.ensias.devpilot.gitserver.workspace.WorkspaceManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GetRepositoryTreeToolTest {

    @TempDir
    Path tempDir;

    private GetRepositoryTreeTool treeTool;
    private Path repository;

    @BeforeEach
    void setUp() throws IOException {

        WorkspaceManager workspaceManager =
                new WorkspaceManager(tempDir.toString());

        workspaceManager.initialize();

        Path workspace =
                workspaceManager.createInvestigationWorkspace("test-123");

        repository = workspace.resolve("repository");

        Files.createDirectories(repository);

        treeTool = new GetRepositoryTreeTool(workspaceManager);
    }

    @Test
    void shouldReturnRepositoryFilesAndDirectories() throws IOException {

        Files.createDirectories(repository.resolve("src/main"));
        Files.writeString(
                repository.resolve("README.md"),
                "Hello LogMind"
        );
        Files.writeString(
                repository.resolve("src/main/App.java"),
                "public class App {}"
        );

        String result = treeTool.getRepositoryTree("test-123");

        assertTrue(result.contains("README.md"));
        assertTrue(result.contains("src"));
        assertTrue(result.contains("App.java"));
    }

    @Test
    void shouldIgnoreGitDirectory() throws IOException {

        Files.createDirectories(repository.resolve(".git"));
        Files.writeString(
                repository.resolve(".git/config"),
                "git configuration"
        );

        Files.writeString(
                repository.resolve("README.md"),
                "Hello"
        );

        String result = treeTool.getRepositoryTree("test-123");

        assertTrue(result.contains("README.md"));
        assertFalse(result.contains(".git"));
    }

    @Test
    void shouldRejectMissingRepository() throws IOException {

        Files.delete(repository);

        assertThrows(IllegalStateException.class, () ->
                treeTool.getRepositoryTree("test-123")
        );
    }
}
