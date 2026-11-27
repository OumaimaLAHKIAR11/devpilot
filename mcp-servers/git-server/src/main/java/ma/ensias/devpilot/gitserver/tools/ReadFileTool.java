
package ma.ensias.devpilot.gitserver.tools;

import ma.ensias.devpilot.gitserver.workspace.WorkspaceManager;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

@Component
public class ReadFileTool {

    private final WorkspaceManager workspaceManager;

    private static final long MAX_FILE_SIZE = 200 * 1024;

    public ReadFileTool(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @McpTool(
            name = "read_file",
            description = "Reads the content of a file from the Git repository " +
                    "associated with an investigation. Only relative paths " +
                    "inside the repository are allowed."
    )
    public String readFile(
            @McpToolParam(
                    description = "Unique identifier of the investigation"
            )
            String investigationId,

            @McpToolParam(
                    description = "Relative path of the file inside the repository"
            )
            String filePath
    ) throws IOException {

        // 1. Retrieve the investigation workspace
        Path workspace =
                workspaceManager.createInvestigationWorkspace(investigationId);

        // 2. Locate the repository
        Path repository = workspace.resolve("repository");

        if (Files.isSymbolicLink(repository)
                || !Files.isDirectory(repository, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalStateException("Repository not found");
        }

        Path realRepository = repository.toRealPath();

        if (!realRepository.startsWith(workspace.toRealPath())) {
            throw new SecurityException("Repository outside workspace");
        }

        // 3. Validate the requested file path
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        Path relativePath = Path.of(filePath);

        if (relativePath.isAbsolute()) {
            throw new SecurityException("Absolute paths are not allowed");
        }

        for (Path part : relativePath) {
            if (part.toString().equals("..")) {
                throw new SecurityException("Path traversal is forbidden");
            }
        }

        // 4. Resolve and normalize the requested path
        Path target = realRepository.resolve(relativePath).normalize();

        if (!target.startsWith(realRepository)
                || target.equals(realRepository)) {
            throw new SecurityException("Access outside repository is forbidden");
        }

        // 5. Reject symbolic links in every path component
        Path current = realRepository;

        for (Path part : realRepository.relativize(target)) {
            current = current.resolve(part);

            if (Files.isSymbolicLink(current)) {
                throw new SecurityException("Symbolic links are not allowed");
            }
        }

        // 6. Check that the target is a regular file
        if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("File not found or not a regular file");
        }

        // 7. Check the file size
        long fileSize = Files.size(target);

        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File exceeds the maximum allowed size of 200 KB"
            );
        }

        // 8. Read the file content
        byte[] content = Files.readAllBytes(target);

        // 9. Reject binary files
        for (byte value : content) {
            if (value == 0) {
                throw new IllegalArgumentException(
                        "Binary files cannot be read as text"
                );
            }
        }

        return new String(content, StandardCharsets.UTF_8);
    }
}
