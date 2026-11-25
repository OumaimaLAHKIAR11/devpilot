package ma.ensias.devpilot.knowledge.api;

import java.util.List;

public record SearchResult(
        List<KnowledgeHit> hits,
        boolean sufficientEvidence
) {
}