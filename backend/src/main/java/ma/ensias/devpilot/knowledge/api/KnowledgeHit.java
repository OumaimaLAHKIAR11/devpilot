package ma.ensias.devpilot.knowledge.api;

import java.util.UUID;

public record KnowledgeHit(
        UUID chunkId,
        UUID documentId,
        String documentTitle,
        String source,
        String version,
        String technology,
        String excerpt,
        double score,
        ScoreKind scoreKind
) {
}