
package ma.ensias.devpilot.gitserver.git;

import ma.ensias.devpilot.gitserver.workspace.WorkspaceManager;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class GitRepositoryManager {

    private final WorkspaceManager workspaceManager;

    private static final Pattern VALID_COMMIT =
            Pattern.compile("[a-fA-F0-9]{7,64}");

    private static final Duration GIT_TIMEOUT =
            Duration.ofMinutes(2);

    public GitRepositoryManager(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    public Path cloneRepository(
            String investigationId,
            String repositoryUrl,
            String commitSha
    ) throws IOException, InterruptedException {

        // 1. Validate the commit SHA
        if (commitSha == null
                || !VALID_COMMIT.matcher(commitSha).matches()) {
            throw new IllegalArgumentException("Invalid commit SHA");
        }

        // 2. Validate the repository URL
        validateRepositoryUrl(repositoryUrl);

        // 3. Create an isolated workspace
        Path investigationWorkspace =
                workspaceManager.createInvestigationWorkspace(
                        investigationId
                );

        Path repositoryPath =
                investigationWorkspace.resolve("repository");

        // 4. Prevent overwriting an existing repository
        if (Files.exists(repositoryPath)) {
            throw new IllegalStateException(
                    "Repository already exists in this workspace"
            );
        }

        // 5. Clone the repository without checking out files yet
        runGitCommand(
                investigationWorkspace,
                List.of(
                        "clone",
                        "--no-checkout",
                        "--",
                        repositoryUrl,
                        repositoryPath.toString()
                )
        );

        // 6. Checkout the requested commit
        runGitCommand(
                repositoryPath,
                List.of(
                        "checkout",
                        "--detach",
                        commitSha
                )
        );

        return repositoryPath;
    }



    void validateRepositoryUrl(String repositoryUrl) {

        if (repositoryUrl == null || repositoryUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Repository URL cannot be empty"
            );
        }

        try {
            URI uri = new URI(repositoryUrl);

            // Only HTTPS repositories are allowed
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException(
                        "Only HTTPS repository URLs are allowed"
                );
            }

            // The URL must contain a hostname
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException(
                        "Invalid repository hostname"
                );
            }

            // Reject URLs containing embedded credentials
            if (uri.getUserInfo() != null) {
                throw new IllegalArgumentException(
                        "Credentials in repository URLs are not allowed"
                );
            }

            // Reject query parameters and fragments
            if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new IllegalArgumentException(
                        "Repository URL cannot contain query parameters or fragments"
                );
            }

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Invalid repository URL format",
                    e
            );
        }
    }



    private void runGitCommand(
            Path workingDirectory,
            List<String> arguments
    ) throws IOException, InterruptedException {

        // 1. Prepare the Git command
        List<String> command = new java.util.ArrayList<>();
        command.add("git");
        command.addAll(arguments);

        // 2. Configure the process
        ProcessBuilder processBuilder =
                new ProcessBuilder(command);

        processBuilder.directory(workingDirectory.toFile());
        processBuilder.redirectErrorStream(true);

        // 3. Start Git
        Process process = processBuilder.start();

        // 4. Read Git output while the process is running
        CompletableFuture<String> outputFuture =
                CompletableFuture.supplyAsync(() -> {
                    try (
                            InputStream input = process.getInputStream();
                            ByteArrayOutputStream output =
                                    new ByteArrayOutputStream()
                    ) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        int maxOutputSize = 1024 * 1024;

                        while ((bytesRead = input.read(buffer)) != -1) {
                            int remaining =
                                    maxOutputSize - output.size();

                            if (remaining > 0) {
                                output.write(
                                        buffer,
                                        0,
                                        Math.min(bytesRead, remaining)
                                );
                            }
                        }

                        return output.toString(StandardCharsets.UTF_8);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        // 5. Wait for Git with a timeout
        boolean completed = process.waitFor(
                GIT_TIMEOUT.toSeconds(),
                TimeUnit.SECONDS
        );

        if (!completed) {
            process.destroyForcibly();
            process.waitFor();

            throw new IOException("Git command timed out");
        }

        // 6. Retrieve the output
        String output;

        try {
            output = outputFuture.join();
        } catch (RuntimeException e) {
            throw new IOException("Could not read Git output", e);
        }

        // 7. Check the result
        if (process.exitValue() != 0) {
            throw new IOException("Git command failed: " + output);
        }
    }

}
