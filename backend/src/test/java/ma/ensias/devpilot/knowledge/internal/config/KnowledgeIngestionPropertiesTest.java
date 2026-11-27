
package ma.ensias.devpilot.knowledge.internal.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeIngestionPropertiesTest {

    @Test
    void shouldCreatePropertiesWithValidValues() {
        // Given
        long maxFileSizeBytes = 10_485_760L;
        int maxExtractedTextChars = 2_000_000;
        List<String> allowedExtensions = List.of("md", "markdown", "txt");

        // When
        KnowledgeIngestionProperties properties =
                new KnowledgeIngestionProperties(
                        maxFileSizeBytes,
                        maxExtractedTextChars,
                        allowedExtensions
                );

        // Then
        assertEquals(maxFileSizeBytes, properties.maxFileSizeBytes());
        assertEquals(maxExtractedTextChars,
                properties.maxExtractedTextChars());
        assertEquals(allowedExtensions, properties.allowedExtensions());
    }

    @Test
    void shouldNormalizeAllowedExtensions() {
        // Given
        List<String> extensions = List.of(
                " MD ",
                ".TXT",
                "markdown",
                "md"
        );

        // When
        KnowledgeIngestionProperties properties =
                new KnowledgeIngestionProperties(
                        10_485_760L,
                        2_000_000,
                        extensions
                );

        // Then
        assertEquals(
                List.of("md", "txt", "markdown"),
                properties.allowedExtensions()
        );
    }

    @Test
    void shouldRejectNonPositiveMaxFileSizeBytes() {
        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new KnowledgeIngestionProperties(
                        0,
                        2_000_000,
                        List.of("md", "txt")
                )
        );
    }

    @Test
    void shouldRejectNonPositiveMaxExtractedTextChars() {
        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new KnowledgeIngestionProperties(
                        10_485_760L,
                        0,
                        List.of("md", "txt")
                )
        );
    }

    @Test
    void shouldRejectNullAllowedExtensions() {
        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new KnowledgeIngestionProperties(
                        10_485_760L,
                        2_000_000,
                        null
                )
        );
    }

    @Test
    void shouldRejectEmptyAllowedExtensions() {
        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new KnowledgeIngestionProperties(
                        10_485_760L,
                        2_000_000,
                        List.of()
                )
        );
    }
}
