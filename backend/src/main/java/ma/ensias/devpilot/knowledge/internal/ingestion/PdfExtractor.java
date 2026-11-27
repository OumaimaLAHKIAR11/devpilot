
package ma.ensias.devpilot.knowledge.internal.ingestion;

import java.io.IOException;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public final class PdfExtractor implements DocumentTextExtractor {

    private final int maxExtractedTextChars;
    private final int maxPdfPages;
    private final SecretMasker secretMasker;

    public PdfExtractor(
            int maxExtractedTextChars,
            int maxPdfPages,
            SecretMasker secretMasker
    ) {
        if (maxExtractedTextChars <= 0 || maxPdfPages <= 0) {
            throw new IllegalArgumentException(
                    "Extraction limits must be positive");
        }

        if (secretMasker == null) {
            throw new IllegalArgumentException(
                    "secretMasker is required");
        }

        this.maxExtractedTextChars = maxExtractedTextChars;
        this.maxPdfPages = maxPdfPages;
        this.secretMasker = secretMasker;
    }

    @Override
    public ExtractedContent extract(
            String filename,
            byte[] content
    ) {
        if (filename == null
                || filename.isBlank()
                || !filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new ContentExtractionException(
                    "Invalid PDF filename");
        }

        if (content == null || content.length == 0) {
            throw new ContentExtractionException(
                    "File content is empty");
        }

        try (PDDocument document = Loader.loadPDF(content)) {

            if (document.isEncrypted()) {
                throw new ContentExtractionException(
                        "Encrypted PDF is not supported");
            }

            int pages = document.getNumberOfPages();

            if (pages == 0 || pages > maxPdfPages) {
                throw new ContentExtractionException(
                        "PDF page count is outside the allowed range");
            }

            String rawText = new PDFTextStripper().getText(document);

            String cleanedText = TextCleaner.clean(rawText);

            if (cleanedText.isBlank()) {
                throw new ContentExtractionException(
                        "PDF contains no native extractable text");
            }

            String maskedText = secretMasker.mask(cleanedText);

            if (maskedText.length() > maxExtractedTextChars) {
                throw new ContentExtractionException(
                        "Extracted text exceeds configured limit");
            }

            return new ExtractedContent(maskedText, "pdf");

        } catch (ContentExtractionException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new ContentExtractionException(
                    "PDF cannot be parsed",
                    exception
            );
        }
    }
}
