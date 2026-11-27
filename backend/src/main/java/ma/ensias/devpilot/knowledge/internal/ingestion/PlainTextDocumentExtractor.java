
package ma.ensias.devpilot.knowledge.internal.ingestion;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

public final class PlainTextDocumentExtractor
        implements DocumentTextExtractor {

    private static final Set<String> SUPPORTED =
            Set.of("md", "markdown", "txt");

    private final int maxExtractedTextChars;

    public PlainTextDocumentExtractor(int maxExtractedTextChars) {
        if (maxExtractedTextChars <= 0) {
            throw new IllegalArgumentException(
                    "maxExtractedTextChars must be positive");
        }
        this.maxExtractedTextChars = maxExtractedTextChars;
    }

    @Override
    public ExtractedContent extract(String filename, byte[] content) {
        if (filename == null || filename.isBlank()) {
            throw new ContentExtractionException(
                    "Filename is required");
        }

        if (content == null || content.length == 0) {
            throw new ContentExtractionException(
                    "File content is empty");
        }

        String extension = extensionOf(filename);

        if (!SUPPORTED.contains(extension)) {
            throw new ContentExtractionException(
                    "Unsupported file extension: " + extension);
        }

        final String decoded;

        try {
            decoded = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(content))
                    .toString();
        } catch (CharacterCodingException ex) {
            throw new ContentExtractionException(
                    "File is not valid UTF-8", ex);
        }

        String cleaned = TextCleaner.clean(decoded);

        if (cleaned.isBlank()) {
            throw new ContentExtractionException(
                    "Extracted text is empty");
        }

        if (cleaned.length() > maxExtractedTextChars) {
            throw new ContentExtractionException(
                    "Extracted text exceeds configured limit");
        }

        return new ExtractedContent(cleaned, extension);
    }

    private static String extensionOf(String filename) {
        String normalized = filename.trim()
                .toLowerCase(Locale.ROOT);

        int slash = Math.max(
                normalized.lastIndexOf('/'),
                normalized.lastIndexOf('\\')
        );

        String basename = normalized.substring(slash + 1);
        int dot = basename.lastIndexOf('.');

        if (dot <= 0 || dot == basename.length() - 1) {
            throw new ContentExtractionException(
                    "Filename has no supported extension");
        }

        return basename.substring(dot + 1);
    }
}
