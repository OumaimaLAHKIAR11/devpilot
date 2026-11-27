
package ma.ensias.devpilot.controller;

import ma.ensias.devpilot.agent.analysis.LogInput;
import ma.ensias.devpilot.agent.analysis.LogAnalysisAgent;
import ma.ensias.devpilot.agent.analysis.LogAnalysisResult;
import ma.ensias.devpilot.agent.core.AgentContext;
import ma.ensias.devpilot.agent.core.AgentExecutionResult;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents")
public class LogAnalysisController {

    private final LogAnalysisAgent logAnalysisAgent;

    public LogAnalysisController(LogAnalysisAgent logAnalysisAgent) {
        this.logAnalysisAgent = logAnalysisAgent;
    }

    @PostMapping("/log-analysis")
    public AgentExecutionResult<LogAnalysisResult> analyzeLogs(
            @RequestBody LogInput input) {

        return logAnalysisAgent.execute(
                input,
                new AgentContext()
        );
    }
}
