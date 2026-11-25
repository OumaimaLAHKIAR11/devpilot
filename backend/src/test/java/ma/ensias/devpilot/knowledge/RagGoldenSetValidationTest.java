
package ma.ensias.devpilot.knowledge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RagGoldenSetValidationTest {

    private static final Path GOLDEN_SET =
            Path.of("..", "knowledge", "evaluation", "golden-set-v1.jsonl")
                    .toAbsolutePath()
                    .normalize();

    private static final Set<String> REQUIRED_FIELDS = Set.of(
            "id", "source", "repository", "build_id",
            "technology", "query", "failure_excerpt",
            "answerable", "relevant_docs", "notes"
    );

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void goldenSetMustContainTwentyValidCases() throws Exception {
        assertTrue(
                Files.isRegularFile(GOLDEN_SET),
                "Golden Set file not found: " + GOLDEN_SET
        );

        Set<String> ids = new HashSet<>();

        try (Stream<String> lines = Files.lines(GOLDEN_SET)) {
            var cases = lines
                    .filter(line -> !line.isBlank())
                    .map(line -> {
                        try {
                            return mapper.readTree(line);
                        } catch (Exception e) {
                            throw new IllegalArgumentException(
                                    "Invalid JSONL line: " + line, e
                            );
                        }
                    })
                    .toList();

            assertEquals(20, cases.size(),
                    "Golden Set must contain exactly 20 cases");

            for (JsonNode item : cases) {
                validateCase(item, ids);
            }

            assertEquals(20, ids.size(), "Case IDs must be unique");
        }
    }

    private void validateCase(JsonNode item, Set<String> ids) {
        for (String field : REQUIRED_FIELDS) {
            assertTrue(item.has(field),
                    "Missing field: " + field);
        }

        assertTrue(item.get("id").isTextual());
        assertFalse(item.get("id").asText().isBlank());
        assertTrue(ids.add(item.get("id").asText()),
                "Duplicate case ID: " + item.get("id").asText());

        for (String field : Set.of(
                "source", "repository", "build_id",
                "query", "failure_excerpt", "notes"
        )) {
            assertTrue(item.get(field).isTextual(),
                    field + " must be a string");
            assertFalse(item.get(field).asText().isBlank(),
                    field + " must not be blank");
        }

        assertTrue(item.get("technology").isArray());
        assertFalse(item.get("technology").isEmpty());

        assertTrue(item.get("answerable").isBoolean());
        assertTrue(item.get("relevant_docs").isArray());

        boolean answerable = item.get("answerable").asBoolean();
        boolean hasRelevantDocs = !item.get("relevant_docs").isEmpty();

        if (answerable) {
            assertTrue(hasRelevantDocs,
                    "Answerable case must reference relevant documents");
        } else {
            assertFalse(hasRelevantDocs,
                    "Unanswerable case must have no relevant documents");
        }
    }
}
