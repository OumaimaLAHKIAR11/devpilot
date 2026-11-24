package ma.ensias.devpilot.gitserver.tools;

import ma.ensias.devpilot.gitserver.workspace.WorkspaceManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReadFileToolTest {

    @TempDir
    Path tempDir;

    private ReadFileTool readFileTool;
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

        readFileTool = new ReadFileTool(workspaceManager);
    }

    @Test
    void shouldReadExistingFile() throws IOException {

        Files.writeString(
                repository.resolve("README.md"),
                "Hello LogMind"
        );

        String result =
                readFileTool.readFile("test-123", "README.md");

        assertEquals("Hello LogMind", result);
    }

    @Test
    void shouldRejectMissingFile() {

        assertThrows(IllegalArgumentException.class, () ->
                readFileTool.readFile("test-123", "missing.txt")
        );
    }

    @Test
    void shouldRejectPathTraversal() {

        assertThrows(SecurityException.class, () ->
                readFileTool.readFile("test-123", "../outside.txt")
        );
    }

    @Test
    void shouldRejectAbsolutePath() {

        assertThrows(SecurityException.class, () ->
                readFileTool.readFile(
                        "test-123",
                        tempDir.resolve("outside.txt").toString()
                )
        );
    }

    @Test
    void shouldRejectDirectoryInsteadOfFile() throws IOException {

        Files.createDirectories(repository.resolve("src"));

        assertThrows(IllegalArgumentException.class, () ->
                readFileTool.readFile("test-123", "src")
        );
    }
}