
package ma.ensias.devpilot.knowledge.internal.ingestion;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SecretMasker {

    private static final String MASK = "[REDACTED]";

    private static final List<Pattern> TOKEN_PATTERNS = List.of(
            Pattern.compile("\\bgh[pousr]_[A-Za-z0-9]{20,}\\b"),
            Pattern.compile("\\bgithub_pat_[A-Za-z0-9_]{20,}\\b"),
            Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b")
    );

    private static final Pattern BEARER_PATTERN = Pattern.compile(
            "(?i)(Bearer\\s+)([A-Za-z0-9._~+/=-]{8,})"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "(?i)(password\\s*[:=]\\s*)([^\\s,;]{8,})"
    );

    private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile(
            "-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----"
                    + "[\\s\\S]*?"
                    + "-----END [A-Z0-9 ]*PRIVATE KEY-----"
    );

    public String mask(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;

        for (Pattern pattern : TOKEN_PATTERNS) {
            result = pattern.matcher(result).replaceAll(MASK);
        }

        result = replacePreservingPrefix(result, BEARER_PATTERN);
        result = replacePreservingPrefix(result, PASSWORD_PATTERN);

        result = PRIVATE_KEY_PATTERN.matcher(result)
                .replaceAll(MASK);

        return result;
    }

    private String replacePreservingPrefix(
            String input,
            Pattern pattern
    ) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer output = new StringBuffer();

        while (matcher.find()) {
            String replacement = matcher.group(1) + MASK;

            matcher.appendReplacement(
                    output,
                    Matcher.quoteReplacement(replacement)
            );
        }

        matcher.appendTail(output);

        return output.toString();
    }
}
