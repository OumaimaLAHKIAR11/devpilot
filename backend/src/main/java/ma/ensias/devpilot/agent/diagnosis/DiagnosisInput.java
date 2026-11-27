
package ma.ensias.devpilot.agent.diagnosis;

import ma.ensias.devpilot.agent.analysis.LogAnalysisResult;

public class DiagnosisInput {

    private String batchId;
    private LogAnalysisResult logAnalysis;
    private String codeContext;

    public DiagnosisInput(
            String batchId,
            LogAnalysisResult logAnalysis,
            String codeContext) {

        this.batchId = batchId;
        this.logAnalysis = logAnalysis;
        this.codeContext = codeContext;
    }

    public String getBatchId() {
        return batchId;
    }

    public LogAnalysisResult getLogAnalysis() {
        return logAnalysis;
    }

    public String getCodeContext() {
        return codeContext;
    }
}
