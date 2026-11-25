
package ma.ensias.devpilot.knowledge.internal.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import java.util.List;
import java.util.Objects;

@Component
@Profile("!test")
public class SpringAiTextEmbedder implements TextEmbedder {

    private final EmbeddingModel embeddingModel;
    private final String modelId;
    private final int dimension;

    public SpringAiTextEmbedder(
            EmbeddingModel embeddingModel,
            @Value("${devpilot.knowledge.embedding.model-id}")
            String modelId,
            @Value("${devpilot.knowledge.embedding.dimension}")
            int dimension
    ) {
        this.embeddingModel = Objects.requireNonNull(embeddingModel);
        this.modelId = modelId;
        this.dimension = dimension;

        if (dimension <= 0) {
            throw new IllegalArgumentException(
                    "Embedding dimension must be positive"
            );
        }
    }

    @Override
    public float[] embed(String text) {
        validateText(text);

        float[] vector = embeddingModel.embed(text);

        validateVector(vector);

        return vector;
    }

    @Override
    public List<float[]> embedAll(List<String> texts) {
        Objects.requireNonNull(texts, "Texts must not be null");

        if (texts.isEmpty()) {
            return List.of();
        }

        texts.forEach(this::validateText);

        List<float[]> vectors = embeddingModel
                .embedForResponse(texts)
                .getResults()
                .stream()
                .map(result -> result.getOutput())
                .toList();

        if (vectors.size() != texts.size()) {
            throw new IllegalStateException(
                    "Embedding count does not match input count"
            );
        }

        vectors.forEach(this::validateVector);

        return vectors;
    }

    @Override
    public String modelId() {
        return modelId;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    private void validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Text must not be null or blank"
            );
        }
    }

    private void validateVector(float[] vector) {
        if (vector == null || vector.length != dimension) {
            throw new IllegalStateException(
                    "Expected embedding dimension " + dimension
                            + " but received "
                            + (vector == null ? "null" : vector.length)
            );
        }

        for (float value : vector) {
            if (!Float.isFinite(value)) {
                throw new IllegalStateException(
                        "Embedding contains a non-finite value"
                );
            }
        }
    }
}
