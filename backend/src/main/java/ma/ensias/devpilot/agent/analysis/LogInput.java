
package ma.ensias.devpilot.agent.analysis;

public class LogInput {

    private String batchId;
    private String rawLogs;

    public LogInput(String batchId, String rawLogs) {
        this.batchId = batchId;
        this.rawLogs = rawLogs;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getRawLogs() {
        return rawLogs;
    }
}

