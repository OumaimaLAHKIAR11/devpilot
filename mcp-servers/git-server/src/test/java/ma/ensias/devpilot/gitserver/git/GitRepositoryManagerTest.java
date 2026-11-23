package ma.ensias.devpilot.gitserver.git;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitRepositoryManagerTest {

    private GitRepositoryManager gitRepositoryManager;

    @BeforeEach
    void setUp() {
        gitRepositoryManager =
                new GitRepositoryManager(null);
    }

    @Test
    void shouldAcceptValidHttpsRepositoryUrl() {
        assertDoesNotThrow(() ->
                gitRepositoryManager.validateRepositoryUrl(
                        "https://github.com/user/project.git"
                )
        );
    }

    @Test
    void shouldRejectHttpRepositoryUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                gitRepositoryManager.validateRepositoryUrl(
                        "http://github.com/user/project.git"
                )
        );
    }

    @Test
    void shouldRejectFileRepositoryUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                gitRepositoryManager.validateRepositoryUrl(
                        "file:///C:/project"
                )
        );
    }

    @Test
    void shouldRejectEmptyRepositoryUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                gitRepositoryManager.validateRepositoryUrl("")
        );
    }

    @Test
    void shouldRejectUrlContainingCredentials() {
        assertThrows(IllegalArgumentException.class, () ->
                gitRepositoryManager.validateRepositoryUrl(
                        "https://user:password@github.com/project.git"
                )
        );
    }
}