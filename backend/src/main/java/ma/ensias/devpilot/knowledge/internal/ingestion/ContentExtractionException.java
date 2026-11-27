
package ma.ensias.devpilot.knowledge.internal.ingestion;

public class ContentExtractionException extends RuntimeException {

    public ContentExtractionException(String message) {
        super(message);
    }

    public ContentExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
