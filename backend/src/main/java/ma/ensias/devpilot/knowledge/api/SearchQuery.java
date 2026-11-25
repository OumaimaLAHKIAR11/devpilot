package ma.ensias.devpilot.knowledge.api;

import java.util.UUID;

public record SearchQuery(
        String text,
        String technology,
        UUID projectId,
        int topK
) {
    public SearchQuery {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required");
        }

        if (topK < 1 || topK > 20) {
            throw new IllegalArgumentException("topK must be in [1,20]");
        }
    }
}