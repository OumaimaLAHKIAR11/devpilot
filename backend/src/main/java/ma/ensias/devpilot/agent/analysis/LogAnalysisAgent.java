
package ma.ensias.devpilot.agent.analysis;

import tools.jackson.databind.ObjectMapper;

import ma.ensias.devpilot.ai.LlmService;
import ma.ensias.devpilot.agent.core.Agent;
import ma.ensias.devpilot.agent.core.AgentContext;
import ma.ensias.devpilot.agent.core.AgentExecutionResult;
import ma.ensias.devpilot.agent.core.AgentException;
import ma.ensias.devpilot.agent.core.AgentStatus;

import org.springframework.stereotype.Component;

@Component
public class LogAnalysisAgent
        implements Agent<LogInput, LogAnalysisResult> {

    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    public LogAnalysisAgent(
            LlmService llmService,
            ObjectMapper objectMapper) {

        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AgentExecutionResult<LogAnalysisResult> execute(
            LogInput input,
            AgentContext context) {

        // 1. Validate input
        if (input == null) {
            throw new AgentException("LogInput cannot be null");
        }

        if (input.getRawLogs() == null
                || input.getRawLogs().isBlank()) {
            throw new AgentException("Logs cannot be empty");
        }

        // 2. Build the prompt
        String prompt = buildPrompt(input);

        try {
            // 3. Send the prompt to Azure OpenAI
            String response = llmService.generate(prompt);

            // 4. Validate LLM response
            if (response == null || response.isBlank()) {
                throw new AgentException(
                        "The LLM returned an empty response"
                );
            }

            // 5. Convert JSON response into LogAnalysisResult
            LogAnalysisResult result = objectMapper.readValue(
                    response,
                    LogAnalysisResult.class
            );

            // 6. Return successful execution result
            return new AgentExecutionResult<>(
                    AgentStatus.SUCCESS,
                    result,
                    null
            );

        } catch (AgentException e) {
            throw e;

        } catch (Exception e) {
            throw new AgentException(
                    "Log analysis failed: " + e.getMessage(),
                    e
            );
        }
    }

    // Build the prompt sent to the LLM
    private String buildPrompt(LogInput input) {

        return """
                You are an expert software build log analyzer.

                Your task is to analyze the provided build logs and identify
                the main cause of the failure.

                Instructions:
                1. Identify the main error responsible for the build failure.
                2. List other relevant errors related to the main error.
                3. Identify the affected source file, if available.
                4. Identify the affected line number, if available.
                5. Identify the affected dependency, if available.
                6. Identify the failed command, if available.
                7. Provide a concise summary of your analysis.

                Important rules:
                - Focus on the root cause, not only the final error message.
                - Do not invent information that is absent from the logs.
                - If a field cannot be determined, use null.
                - If there are no related errors, return an empty array.
                - Return only valid JSON, without Markdown or explanations.
                - Return the exact Batch ID provided below.

                Expected JSON structure:
                {
                  "batchId": "same batch ID provided below",
                  "mainError": "string or null",
                  "relatedErrors": [],
                  "affectedFile": "string or null",
                  "affectedLine": null,
                  "affectedDependency": "string or null",
                  "failedCommand": "string or null",
                  "summary": "string"
                }

                Batch ID: %s

                Build logs:
                %s
                """.formatted(
                input.getBatchId(),
                input.getRawLogs()
        );
    }
}
