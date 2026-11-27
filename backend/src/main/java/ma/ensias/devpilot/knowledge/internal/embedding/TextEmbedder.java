
package ma.ensias.devpilot.knowledge.internal.embedding;

import java.util.List;

/**
 * Abstraction for text embedding providers.
 * This interface keeps the Knowledge module independent
 * from the underlying embedding provider.
 */
public interface TextEmbedder {

    /**
     * Generates an embedding vector for a single text.
     *
     * @param text input text
     * @return embedding vector
     */
    float[] embed(String text);

    /**
     * Generates embedding vectors for multiple texts.
     *
     * @param texts input texts
     * @return list of embedding vectors
     */
    List<float[]> embedAll(List<String> texts);

    /**
     * Returns the embedding model identifier.
     *
     * @return model identifier
     */
    String modelId();

    /**
     * Returns the expected embedding dimension.
     *
     * @return vector dimension
     */
    int dimension();
}
