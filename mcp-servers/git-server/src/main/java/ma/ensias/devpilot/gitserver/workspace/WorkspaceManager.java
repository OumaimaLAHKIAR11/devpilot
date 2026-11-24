
package ma.ensias.devpilot.gitserver.workspace;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.nio.file.LinkOption;


@Component
public class WorkspaceManager {

    private final Path workspaceRoot;
    private Path realWorkspaceRoot;
    private static final Pattern VALID_ID =
            Pattern.compile("[a-zA-Z0-9_-]{1,64}");

    public WorkspaceManager(
            @Value("${devpilot.execution.workspace-dir:./workspace}")
            String workspaceDirectory
    ) {
        this.workspaceRoot = Path.of(workspaceDirectory)
                .toAbsolutePath()
                .normalize();
    }

    @PostConstruct
    public void initialize() {
        try {
            Files.createDirectories(workspaceRoot);
            realWorkspaceRoot = workspaceRoot.toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to initialize workspace directory: " + workspaceRoot,
                    e
            );
        }
    }



    public Path createInvestigationWorkspace(String investigationId)
            throws IOException {

        // 1. Validate the investigation ID
        if (investigationId == null
                || !VALID_ID.matcher(investigationId).matches()) {
            throw new IllegalArgumentException(
                    "Invalid investigation ID: " + investigationId
            );
        }

        // 2. Build the investigation path
        Path investigationPath = workspaceRoot.resolve(investigationId)
                .normalize();

        // 3. Check that the path stays inside the workspace root
        if (!investigationPath.startsWith(workspaceRoot)
                || investigationPath.equals(workspaceRoot)) {
            throw new SecurityException(
                    "Access outside workspace is forbidden"
            );
        }

        // 4. Reject symbolic links
        if (Files.isSymbolicLink(investigationPath)) {
            throw new SecurityException(
                    "Symbolic links are not allowed"
            );
        }

        // 5. Create the directory
        Files.createDirectories(investigationPath);

        // 6. Resolve the actual physical path
        Path realInvestigationPath = investigationPath.toRealPath();

        // 7. Verify that the real path stays inside the real root
        if (!realInvestigationPath.startsWith(realWorkspaceRoot)
                || realInvestigationPath.equals(realWorkspaceRoot)) {
            throw new SecurityException(
                    "Investigation workspace is outside the allowed directory"
            );
        }

        return realInvestigationPath;
    }


}
