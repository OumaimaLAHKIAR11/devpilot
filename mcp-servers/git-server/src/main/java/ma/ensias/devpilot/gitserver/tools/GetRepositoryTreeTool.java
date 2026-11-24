package ma.ensias.devpilot.gitserver.tools;

import ma.ensias.devpilot.gitserver.workspace.WorkspaceManager;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitOption;
import java.util.EnumSet;

@Component
public class GetRepositoryTreeTool {

    private final WorkspaceManager workspaceManager;

    private static final int MAX_DEPTH = 5;
    private static final int MAX_ENTRIES = 200;

    public GetRepositoryTreeTool(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @McpTool(
            name = "get_repository_tree",
            description = "Returns the directory tree of the Git repository " +
                    "associated with an investigation. " +
                    "Useful for understanding the project structure."
    )
    public String getRepositoryTree(
            @McpToolParam(
                    description = "Unique identifier of the investigation"
            )
            String investigationId
    ) throws IOException {

        // 1. Retrieve the investigation workspace
        Path workspace =
                workspaceManager.createInvestigationWorkspace(investigationId);

        // 2. Locate the repository
        Path repository = workspace.resolve("repository").normalize();

        if (!repository.startsWith(workspace)
                || repository.equals(workspace)) {
            throw new SecurityException("Invalid repository path");
        }

        // 3. Check that the repository exists and is not a symbolic link
        if (Files.isSymbolicLink(repository)
                || !Files.isDirectory(repository, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalStateException(
                    "Repository not found for investigation: " + investigationId
            );
        }

        // 4. Resolve and verify the real repository path
        Path realRepository = repository.toRealPath();

        if (!realRepository.startsWith(workspace)) {
            throw new SecurityException(
                    "Repository is outside the investigation workspace"
            );
        }

        // 5. Build the repository tree
        StringBuilder tree = new StringBuilder();
        tree.append("Repository: ").append(investigationId).append("\n");

        int[] entryCount = {0};

        Files.walkFileTree(
                realRepository,
                EnumSet.noneOf(FileVisitOption.class),
                MAX_DEPTH,
                new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult preVisitDirectory(
                            Path directory,
                            BasicFileAttributes attributes
                    ) throws IOException {

                        if (attributes.isSymbolicLink()
                                || Files.isSymbolicLink(directory)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        if (!directory.equals(realRepository)
                                && directory.getFileName().toString().equals(".git")) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        if (!directory.equals(realRepository)) {
                            if (entryCount[0] >= MAX_ENTRIES) {
                                tree.append("... (result truncated)\n");
                                return FileVisitResult.TERMINATE;
                            }

                            appendEntry(directory, true);
                        }

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                            Path file,
                            BasicFileAttributes attributes
                    ) {

                        if (attributes.isSymbolicLink()
                                || Files.isSymbolicLink(file)) {
                            return FileVisitResult.CONTINUE;
                        }

                        if (entryCount[0] >= MAX_ENTRIES) {
                            tree.append("... (result truncated)\n");
                            return FileVisitResult.TERMINATE;
                        }

                        appendEntry(file, false);

                        return FileVisitResult.CONTINUE;
                    }

                    private void appendEntry(Path path, boolean directory) {
                        Path relativePath = realRepository.relativize(path);

                        String formattedPath =
                                relativePath.toString().replace('\\', '/');

                        tree.append(formattedPath);

                        if (directory) {
                            tree.append("/");
                        }

                        tree.append("\n");
                        entryCount[0]++;
                    }
                }
        );

        return tree.toString();
    }
}
