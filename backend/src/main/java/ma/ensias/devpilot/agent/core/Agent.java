package ma.ensias.devpilot.agent.core;



public interface Agent<I, O> {

    AgentExecutionResult<O> execute(
            I input,
            AgentContext context
    );

}


