package ma.ensias.devpilot.agent.core;

public class AgentExecutionResult<T> {

    private AgentStatus status;
    private T data;
    private String errorMessage;

    public AgentExecutionResult(
            AgentStatus status,
            T data,
            String errorMessage) {

        this.status = status;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

