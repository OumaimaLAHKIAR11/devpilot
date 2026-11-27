
package ma.ensias.devpilot.agent.analysis;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LogAnalysisResult {

    private String batchId;
    private String mainError;
    private List<String> relatedErrors;
    private String affectedFile;
    private Integer affectedLine;
    private String affectedDependency;
    private String failedCommand;
    private String summary;

    @JsonCreator
    public LogAnalysisResult(
            @JsonProperty("batchId") String batchId,
            @JsonProperty("mainError") String mainError,
            @JsonProperty("relatedErrors") List<String> relatedErrors,
            @JsonProperty("affectedFile") String affectedFile,
            @JsonProperty("affectedLine") Integer affectedLine,
            @JsonProperty("affectedDependency") String affectedDependency,
            @JsonProperty("failedCommand") String failedCommand,
            @JsonProperty("summary") String summary) {

        this.batchId = batchId;
        this.mainError = mainError;
        this.relatedErrors = relatedErrors;
        this.affectedFile = affectedFile;
        this.affectedLine = affectedLine;
        this.affectedDependency = affectedDependency;
        this.failedCommand = failedCommand;
        this.summary = summary;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getMainError() {
        return mainError;
    }

    public List<String> getRelatedErrors() {
        return relatedErrors;
    }

    public String getAffectedFile() {
        return affectedFile;
    }

    public Integer getAffectedLine() {
        return affectedLine;
    }

    public String getAffectedDependency() {
        return affectedDependency;
    }

    public String getFailedCommand() {
        return failedCommand;
    }

    public String getSummary() {
        return summary;
    }
}
