package ma.ensias.devpilot.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ma.ensias.devpilot.knowledge.api.KnowledgeSearch;
import ma.ensias.devpilot.knowledge.api.SearchQuery;
import ma.ensias.devpilot.knowledge.api.SearchResult;

import org.junit.jupiter.api.Test;

abstract class KnowledgeSearchContract {

    abstract KnowledgeSearch subject();

    @Test
    void respectsTopK() {

        SearchResult result = subject().search(
                new SearchQuery(
                        "Maven could not resolve dependencies",
                        null,
                        null,
                        3
                )
        );

        assertThat(result.hits().size()).isLessThanOrEqualTo(3);
    }

    @Test
    void hitsCarryProvenance() {

        SearchResult result = subject().search(
                new SearchQuery(
                        "Maven could not resolve dependencies",
                        null,
                        null,
                        3
                )
        );

        result.hits().forEach(hit -> {
            assertThat(hit.source()).isNotBlank();
            assertThat(hit.documentId()).isNotNull();
            assertThat(hit.scoreKind()).isNotNull();
        });
    }

    @Test
    void unknownTopicSignalsInsufficientEvidence() {

        SearchResult result = subject().search(
                new SearchQuery(
                        "zzz unknown topic qqq",
                        null,
                        null,
                        3
                )
        );

        assertThat(result.hits()).isEmpty();
        assertThat(result.sufficientEvidence()).isFalse();
    }

    @Test
    void invalidQueryIsRejected() {

        assertThatThrownBy(
                () -> new SearchQuery(" ", null, null, 3)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(
                () -> new SearchQuery("x", null, null, 0)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}