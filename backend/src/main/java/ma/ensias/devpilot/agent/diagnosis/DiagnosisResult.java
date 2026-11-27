
package ma.ensias.devpilot.agent.diagnosis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DiagnosisResult {

    private String rootCause;
    private List<String> evidence;
    private List<String> affectedFiles;
    private Double confidence;
    private List<String> alternativeCauses;

    @JsonCreator
    public DiagnosisResult(
            @JsonProperty("rootCause") String rootCause,
            @JsonProperty("evidence") List<String> evidence,
            @JsonProperty("affectedFiles") List<String> affectedFiles,
            @JsonProperty("confidence") Double confidence,
            @JsonProperty("alternativeCauses") List<String> alternativeCauses) {

        this.rootCause = rootCause;
        this.evidence = evidence;
        this.affectedFiles = affectedFiles;
        this.confidence = confidence;
        this.alternativeCauses = alternativeCauses;
    }

    public String getRootCause() {
        return rootCause;
    }

    public List<String> getEvidence() {
        return evidence;
    }

    public List<String> getAffectedFiles() {
        return affectedFiles;
    }

    public Double getConfidence() {
        return confidence;
    }

    public List<String> getAlternativeCauses() {
        return alternativeCauses;
    }
}
