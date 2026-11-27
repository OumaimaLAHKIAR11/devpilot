
package ma.ensias.devpilot.knowledge.internal.persistence;

public final class PgVectors {

    private PgVectors() {
    }

    /**
     * Builds a pgvector text literal, e.g. [0.1,0.2,0.3].
     */
    public static String literal(float[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException(
                    "Vector must not be null or empty"
            );
        }

        StringBuilder result = new StringBuilder("[");

        for (int i = 0; i < vector.length; i++) {
            if (!Float.isFinite(vector[i])) {
                throw new IllegalArgumentException(
                        "Vector contains a non-finite value"
                );
            }

            if (i > 0) {
                result.append(',');
            }

            result.append(vector[i]);
        }

        return result.append(']').toString();
    }
}
