
package ma.ensias.devpilot.knowledge.internal.ingestion;

public record ExtractedContent(
        String text,
        String detectedFormat
) {
    public ExtractedContent {
        if (text == null) {
            throw new IllegalArgumentException("text is required");
        }

        if (detectedFormat == null || detectedFormat.isBlank()) {
            throw new IllegalArgumentException(
                    "detectedFormat is required");
        }
    }
}
