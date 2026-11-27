
package ma.ensias.devpilot.knowledge.internal.ingestion;

public interface DocumentTextExtractor {

    ExtractedContent extract(String filename, byte[] content);
}
