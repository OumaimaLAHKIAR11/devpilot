
package ma.ensias.devpilot.knowledge.internal.ingestion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecretMaskerTest {

    private SecretMasker secretMasker;

    @BeforeEach
    void setUp() {
        secretMasker = new SecretMasker();
    }

    @Test
    void shouldMaskGitHubToken() {
        String input = "token=ghp_123456789012345678901234";
        String result = secretMasker.mask(input);

        assertFalse(result.contains("ghp_"));
        assertTrue(result.contains("[REDACTED]"));
    }

    @Test
    void shouldMaskGitHubFineGrainedToken() {
        String input = "github_pat_12345678901234567890";
        String result = secretMasker.mask(input);

        assertFalse(result.contains("github_pat_"));
        assertEquals("[REDACTED]", result);
    }

    @Test
    void shouldMaskAwsAccessKey() {
        String input = "AWS key: AKIA1234567890ABCDEF";
        String result = secretMasker.mask(input);

        assertFalse(result.contains("AKIA"));
        assertTrue(result.contains("[REDACTED]"));
    }

    @Test
    void shouldMaskBearerToken() {
        String input = "Authorization: Bearer abcdefghijklmnopqrstuvwxyz";
        String result = secretMasker.mask(input);

        assertFalse(result.contains("abcdefghijklmnopqrstuvwxyz"));
        assertTrue(result.contains("Bearer [REDACTED]"));
    }

    @Test
    void shouldMaskPassword() {
        String input = "password=MySecretPassword123";
        String result = secretMasker.mask(input);

        assertFalse(result.contains("MySecretPassword123"));
        assertTrue(result.contains("password=[REDACTED]"));
    }

    @Test
    void shouldMaskPrivateKey() {
        String input = """
                -----BEGIN RSA PRIVATE KEY-----
                ABCDEFGHIJKLMNOPQRSTUVWXYZ
                -----END RSA PRIVATE KEY-----
                """;

        String result = secretMasker.mask(input);

        assertFalse(result.contains("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        assertTrue(result.contains("[REDACTED]"));
    }

    @Test
    void shouldPreserveNormalTextAndShortValues() {
        String input = """
                This is a normal technical document.
                password=abc
                Bearer abc
                """;

        assertEquals(input, secretMasker.mask(input));
    }

    @Test
    void shouldMaskMultipleSecretsInSameText() {
        String input = """
                github_pat_12345678901234567890
                AWS: AKIA1234567890ABCDEF
                password=MySecretPassword123
                Authorization: Bearer abcdefghijklmnopqrstuvwxyz
                """;

        String result = secretMasker.mask(input);

        assertFalse(result.contains("github_pat_"));
        assertFalse(result.contains("AKIA"));
        assertFalse(result.contains("MySecretPassword123"));
        assertFalse(result.contains("abcdefghijklmnopqrstuvwxyz"));

        assertTrue(result.contains("[REDACTED]"));
    }
}
