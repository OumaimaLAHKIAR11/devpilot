
package ma.ensias.devpilot.knowledge.internal.ingestion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfExtractorTest {

    private PdfExtractor pdfExtractor;

    @BeforeEach
    void setUp() {
        pdfExtractor = new PdfExtractor(
                2_000_000,
                200,
                new SecretMasker()
        );
    }

    @Test
    void shouldExtractTextFromValidPdf() throws IOException {
        byte[] pdf = createPdfWithText("DevPilot Knowledge Engine");

        ExtractedContent result =
                pdfExtractor.extract("document.pdf", pdf);

        assertEquals("pdf", result.detectedFormat());
        assertTrue(result.text().contains("DevPilot Knowledge Engine"));
    }

    @Test
    void shouldRejectEncryptedPdf() throws IOException {
        byte[] pdf = createEncryptedPdf();

        assertThrows(
                ContentExtractionException.class,
                () -> pdfExtractor.extract("encrypted.pdf", pdf)
        );
    }

    @Test
    void shouldRejectInvalidPdf() {
        byte[] invalidPdf = "This is not a PDF".getBytes(
                java.nio.charset.StandardCharsets.UTF_8);

        assertThrows(
                ContentExtractionException.class,
                () -> pdfExtractor.extract("invalid.pdf", invalidPdf)
        );
    }

    @Test
    void shouldRejectPdfWithoutExtractableText() throws IOException {
        byte[] pdf = createEmptyPdf();

        assertThrows(
                ContentExtractionException.class,
                () -> pdfExtractor.extract("empty.pdf", pdf)
        );
    }

    @Test
    void shouldRejectPdfExceedingPageLimit() throws IOException {
        byte[] pdf = createPdfWithPages(201);

        assertThrows(
                ContentExtractionException.class,
                () -> pdfExtractor.extract("large.pdf", pdf)
        );
    }

    private byte[] createPdfWithText(String text) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream stream =
                         new PDPageContentStream(document, page)) {

                stream.beginText();
                stream.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                        12
                );
                stream.newLineAtOffset(50, 700);
                stream.showText(text);
                stream.endText();
            }

            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] createEncryptedPdf() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            document.addPage(new PDPage());

            StandardProtectionPolicy policy =
                    new StandardProtectionPolicy(
                            "owner-password",
                            "user-password",
                            new AccessPermission()
                    );

            policy.setEncryptionKeyLength(128);
            document.protect(policy);

            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] createEmptyPdf() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            document.addPage(new PDPage());
            document.save(output);

            return output.toByteArray();
        }
    }

    private byte[] createPdfWithPages(int numberOfPages)
            throws IOException {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            for (int i = 0; i < numberOfPages; i++) {
                document.addPage(new PDPage());
            }

            document.save(output);
            return output.toByteArray();
        }
    }
}
