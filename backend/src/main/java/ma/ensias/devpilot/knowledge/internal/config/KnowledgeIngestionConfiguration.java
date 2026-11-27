
package ma.ensias.devpilot.knowledge.internal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KnowledgeIngestionProperties.class)
public class KnowledgeIngestionConfiguration {
}
