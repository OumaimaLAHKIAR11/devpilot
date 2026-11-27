
package ma.ensias.devpilot.knowledge.internal.ingestion;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TextCleanerTest {

    @Test
    void normalizesWindowsAndOldMacLineEndings() {
        assertEquals(
                "alpha\nbeta\ngamma",
                TextCleaner.clean("alpha\r\nbeta\rgamma")
        );
    }

    @Test
    void removesNullCharacters() {
        assertEquals("ab", TextCleaner.clean("a\u0000b"));
    }

    @Test
    void trimsTrailingWhitespace() {
        assertEquals("line", TextCleaner.clean("line   \t"));
    }

    @Test
    void limitsConsecutiveBlankLinesToTwo() {
        assertEquals(
                "a\n\n\nb",
                TextCleaner.clean("a\n\n\n\n\nb")
        );
    }

    @Test
    void rejectsNullInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TextCleaner.clean(null)
        );
    }

    @Test
    void normalizesUnicodeToNfc() {
        String decomposed = "e\u0301";

        assertEquals(
                "\u00e9",
                TextCleaner.clean(decomposed)
        );
    }

    @Test
    void removesTrailingTabs() {
        assertEquals(
                "hello\nworld",
                TextCleaner.clean("hello\t  \nworld\t")
        );
    }

    @Test
    void removesTrailingBlankLines() {
        assertEquals(
                "a",
                TextCleaner.clean("a\n\n\n")
        );
    }

    @Test
    void preservesMarkdownMarkers() {
        String markdown = """
                # Titre
                - élément
                ```java
                System.out.println("Hello");
                ```
                """;

        String expected = """
                # Titre
                - élément
                ```java
                System.out.println("Hello");
                ```
                """.stripTrailing();

        assertEquals(expected, TextCleaner.clean(markdown));
    }
}
