package ma.ensias.devpilot.knowledge.internal.ingestion;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class PlainTextDocumentExtractorTest {

    private final PlainTextDocumentExtractor extractor =
            new PlainTextDocumentExtractor(100);

    @Test
    void extractsMarkdownAsUtf8() {
        byte[] bytes = "# Titre\nBonjour".getBytes(
                StandardCharsets.UTF_8);

        ExtractedContent result = extractor.extract("guide.md", bytes);

        assertEquals("md", result.detectedFormat());
        assertEquals("# Titre\nBonjour", result.text());
    }

    @Test
    void acceptsSupportedExtensionsCaseInsensitively() {
        byte[] bytes = "contenu".getBytes(StandardCharsets.UTF_8);

        assertEquals(
                "txt",
                extractor.extract("NOTES.TXT", bytes).detectedFormat()
        );

        assertEquals(
                "markdown",
                extractor.extract("guide.MARKDOWN", bytes)
                        .detectedFormat()
        );
    }

    @Test
    void rejectsUnsupportedPdfExtension() {
        byte[] bytes = "contenu".getBytes(StandardCharsets.UTF_8);

        assertThrows(
                ContentExtractionException.class,
                () -> extractor.extract("scan.pdf", bytes)
        );
    }

    @Test
    void rejectsEmptyOrNullContent() {
        assertThrows(
                ContentExtractionException.class,
                () -> extractor.extract("empty.txt", new byte[0])
        );

        assertThrows(
                ContentExtractionException.class,
                () -> extractor.extract("empty.txt", null)
        );
    }

    @Test
    void rejectsInvalidUtf8() {
        byte[] invalid = {(byte) 0xC3, (byte) 0x28};

        ContentExtractionException exception = assertThrows(
                ContentExtractionException.class,
                () -> extractor.extract("bad.txt", invalid)
        );

        assertNotNull(exception.getCause());
    }

    @Test
    void rejectsTextAboveLimitWithoutTruncating() {
        PlainTextDocumentExtractor small =
                new PlainTextDocumentExtractor(3);

        byte[] bytes = "four".getBytes(StandardCharsets.UTF_8);

        assertThrows(
                ContentExtractionException.class,
                () -> small.extract("large.txt", bytes)
        );
    }

    @Test
    void rejectsInvalidFilenamesAndBlankExtractedText() {
        byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);

        // Nom vide ou blanc
        assertThrows(
                ContentExtractionException.class,
                () -> extractor.extract(" ", bytes)
        );

        // Extension sans point
        assertThrows(
                ContentExtractionException.class,
                () -> extractor.extract("README", bytes)
        );

        // Nom caché sans extension
        assertThrows(
                ContentExtractionException.class,
                () -> extractor.extract(".gitignore", bytes)
        );

        // Extension avec espaces autour du nom
        assertEquals(
                "md",
                extractor.extract("  GUIDE.MD  ", bytes).detectedFormat()
        );

        // Contenu uniquement composé d'espaces
        byte[] blank = "   \n\t  ".getBytes(StandardCharsets.UTF_8);

        assertThrows(
                ContentExtractionException.class,
                () -> extractor.extract("blank.txt", blank)
        );
    }
}
