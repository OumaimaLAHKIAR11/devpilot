
package ma.ensias.devpilot.knowledge.internal.embedding;

import java.util.ArrayList;
import java.util.List;

public class FakeTextEmbedder implements TextEmbedder {

    private static final int DIMENSION = 1536;

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Text must not be null or blank"
            );
        }

        float[] vector = new float[DIMENSION];

        // Deterministic vector for testing only.
        int index = Math.floorMod(text.hashCode(), DIMENSION);
        vector[index] = 1.0f;

        return vector;
    }

    @Override
    public List<float[]> embedAll(List<String> texts) {
        if (texts == null) {
            throw new IllegalArgumentException(
                    "Texts must not be null"
            );
        }

        List<float[]> vectors = new ArrayList<>();

        for (String text : texts) {
            vectors.add(embed(text));
        }

        return vectors;
    }

    @Override
    public String modelId() {
        return "fake-embedding";
    }

    @Override
    public int dimension() {
        return DIMENSION;
    }
}
