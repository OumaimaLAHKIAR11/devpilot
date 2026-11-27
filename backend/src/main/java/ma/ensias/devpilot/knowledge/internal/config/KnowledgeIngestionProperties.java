
package ma.ensias.devpilot.knowledge.internal.config;

import java.util.List;
import java.util.Locale;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devpilot.knowledge.ingestion")
public record KnowledgeIngestionProperties(
        long maxFileSizeBytes,
        int maxExtractedTextChars,
        int maxPdfPages,
        List<String> allowedExtensions
) {
    public KnowledgeIngestionProperties {
        if (maxFileSizeBytes <= 0) {
            throw new IllegalArgumentException(
                    "maxFileSizeBytes must be positive");
        }

        if (maxExtractedTextChars <= 0) {
            throw new IllegalArgumentException(
                    "maxExtractedTextChars must be positive");
        }

        if (maxPdfPages <= 0) {
            throw new IllegalArgumentException(
                    "maxPdfPages must be positive");
        }

        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            throw new IllegalArgumentException(
                    "allowedExtensions must not be empty");
        }

        allowedExtensions = allowedExtensions.stream()
                .map(String::trim)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .map(s -> s.startsWith(".") ? s.substring(1) : s)
                .distinct()
                .toList();
    }
}
