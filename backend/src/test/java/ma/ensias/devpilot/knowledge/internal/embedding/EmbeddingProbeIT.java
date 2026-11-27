
package ma.ensias.devpilot.knowledge.internal.embedding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Tag;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnabledIfEnvironmentVariable(
        named = "AZURE_OPENAI_API_KEY",
        matches = ".+"
)
@Tag("azure")
class EmbeddingProbeIT {

    @Autowired
    private TextEmbedder textEmbedder;

    @Test
    void shouldGenerateValidEmbedding() {
        float[] vector = textEmbedder.embed(
                "Maven dependency resolution failed"
        );

        assertNotNull(vector);
        assertEquals(1536, vector.length);
        assertEquals(1536, textEmbedder.dimension());

        assertEquals(
                "text-embedding-3-small",
                textEmbedder.modelId()
        );

        for (float value : vector) {
            assertTrue(Float.isFinite(value));
        }
    }

    @Test
    void shouldGenerateEmbeddingsForMultipleTexts() {
        List<String> texts = List.of(
                "Maven dependency resolution failed",
                "The Maven build cannot resolve its dependencies"
        );

        List<float[]> vectors = textEmbedder.embedAll(texts);

        assertEquals(texts.size(), vectors.size());

        for (float[] vector : vectors) {
            assertNotNull(vector);
            assertEquals(1536, vector.length);
        }
    }

    @Test
    void semanticallySimilarTextsShouldHaveHigherSimilarity() {
        List<String> texts = List.of(
                "Maven dependency resolution failed",
                "The Maven build cannot resolve its dependencies",
                "The weather is sunny and warm today"
        );

        List<float[]> vectors = textEmbedder.embedAll(texts);

        double similarScore = cosineSimilarity(
                vectors.get(0),
                vectors.get(1)
        );

        double unrelatedScore = cosineSimilarity(
                vectors.get(0),
                vectors.get(2)
        );

        System.out.println("Similar text score: " + similarScore);
        System.out.println("Unrelated text score: " + unrelatedScore);

        assertTrue(
                similarScore > unrelatedScore,
                "Semantically similar texts should have higher cosine similarity"
        );
    }

    private double cosineSimilarity(float[] a, float[] b) {
        assertEquals(a.length, b.length);

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            throw new IllegalArgumentException(
                    "Cannot calculate cosine similarity for zero vectors"
            );
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
