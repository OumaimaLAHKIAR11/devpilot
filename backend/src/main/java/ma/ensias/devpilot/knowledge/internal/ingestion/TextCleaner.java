
package ma.ensias.devpilot.knowledge.internal.ingestion;

import java.text.Normalizer;

public final class TextCleaner {

    private TextCleaner() {
    }

    public static String clean(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input is required");
        }

        String normalized = Normalizer.normalize(
                input, Normalizer.Form.NFC);

        normalized = normalized
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace("\u0000", "");

        String[] lines = normalized.split("\n", -1);
        StringBuilder output = new StringBuilder();
        int consecutiveBlankLines = 0;

        for (String line : lines) {
            String cleanedLine = line.stripTrailing();

            if (cleanedLine.isBlank()) {
                consecutiveBlankLines++;

                if (consecutiveBlankLines <= 2) {
                    output.append('\n');
                }
            } else {
                consecutiveBlankLines = 0;
                output.append(cleanedLine).append('\n');
            }
        }

        // Retire uniquement les sauts de ligne terminaux ajoutés
        // par le parcours, sans supprimer le contenu.
        while (!output.isEmpty()
                && output.charAt(output.length() - 1) == '\n') {
            output.setLength(output.length() - 1);
        }

        return output.toString();
    }
}
