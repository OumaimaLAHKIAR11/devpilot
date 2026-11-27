
package ma.ensias.devpilot.knowledge.internal.config;

import ma.ensias.devpilot.knowledge.internal.ingestion.PdfExtractor;
import ma.ensias.devpilot.knowledge.internal.ingestion.SecretMasker;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KnowledgeIngestionProperties.class)
public class KnowledgeIngestionConfiguration {

    @Bean
    public SecretMasker secretMasker() {
        return new SecretMasker();
    }

    @Bean
    public PdfExtractor pdfExtractor(
            KnowledgeIngestionProperties properties,
            SecretMasker secretMasker
    ) {
        return new PdfExtractor(
                properties.maxExtractedTextChars(),
                properties.maxPdfPages(),
                secretMasker
        );
    }
}
