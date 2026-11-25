package ma.ensias.devpilot.knowledge.internal;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ma.ensias.devpilot.knowledge.api.KnowledgeHit;
import ma.ensias.devpilot.knowledge.api.KnowledgeSearch;
import ma.ensias.devpilot.knowledge.api.ScoreKind;
import ma.ensias.devpilot.knowledge.api.SearchQuery;
import ma.ensias.devpilot.knowledge.api.SearchResult;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "devpilot.fakes.knowledge",
        havingValue = "true",
        matchIfMissing = true
)
public class FakeKnowledgeSearch implements KnowledgeSearch {

    @Override
    public SearchResult search(SearchQuery query) {

        String text = query.text().toLowerCase(Locale.ROOT);

        if (!text.contains("maven")) {
            return new SearchResult(List.of(), false);
        }

        KnowledgeHit hit = new KnowledgeHit(
                UUID.nameUUIDFromBytes(
                        "chunk-1".getBytes(StandardCharsets.UTF_8)
                ),
                UUID.nameUUIDFromBytes(
                        "doc-1".getBytes(StandardCharsets.UTF_8)
                ),
                "Maven dependency resolution",
                "maven-dependency-resolution.md",
                "Maven 3.9",
                "MAVEN",
                "Fake excerpt about unresolved dependencies.",
                0.87,
                ScoreKind.VECTOR_SIMILARITY
        );

        return new SearchResult(List.of(hit), true);
    }
}