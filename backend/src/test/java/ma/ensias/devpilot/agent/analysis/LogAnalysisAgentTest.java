
package ma.ensias.devpilot.agent.analysis;

import tools.jackson.databind.ObjectMapper;

import ma.ensias.devpilot.ai.LlmService;
import ma.ensias.devpilot.agent.core.AgentContext;
import ma.ensias.devpilot.agent.core.AgentExecutionResult;
import ma.ensias.devpilot.agent.core.AgentStatus;
import ma.ensias.devpilot.agent.core.AgentException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogAnalysisAgentTest {

    @Mock
    private LlmService llmService;

    private LogAnalysisAgent logAnalysisAgent;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        logAnalysisAgent = new LogAnalysisAgent(
                llmService,
                objectMapper
        );
    }

    @Test
    void shouldAnalyzeLogsSuccessfully() throws Exception {

        // 1. Prepare input logs
        LogInput input = new LogInput(
                "batch-123",
                """
                [ERROR] COMPILATION ERROR
                UserService.java:[25,10]
                cannot find symbol: variable user
                [INFO] BUILD FAILURE
                """
        );

        // 2. Simulate the LLM response
        String llmResponse = """
                {
                    "batchId": "batch-123",
                    "mainError": "Cannot find symbol: variable user",
                    "relatedErrors": [],
                    "affectedFile": "UserService.java",
                    "affectedLine": 25,
                    "affectedDependency": null,
                    "failedCommand": "mvn clean install",
                    "summary": "Compilation failed because the variable user is undefined."
                }
                """;

        when(llmService.generate(anyString()))
                .thenReturn(llmResponse);

        // 3. Execute the agent
        AgentExecutionResult<LogAnalysisResult> result =
                logAnalysisAgent.execute(
                        input,
                        new AgentContext()
                );

        // 4. Verify the result
        assertNotNull(result);
        assertEquals(AgentStatus.SUCCESS, result.getStatus());

        assertNotNull(result.getData());

        assertEquals(
                "batch-123",
                result.getData().getBatchId()
        );

        assertEquals(
                "UserService.java",
                result.getData().getAffectedFile()
        );

        assertEquals(
                25,
                result.getData().getAffectedLine()
        );

        // 5. Verify that the LLM was called
        verify(llmService, times(1)).generate(anyString());
    }

    @Test
    void shouldThrowExceptionWhenLogsAreEmpty() {

        // Arrange
        LogInput input = new LogInput(
                "batch-124",
                ""
        );

        // Act & Assert
        assertThrows(
                AgentException.class,
                () -> logAnalysisAgent.execute(
                        input,
                        new AgentContext()
                )
        );

        // Verify that the LLM was not called
        verifyNoInteractions(llmService);
    }

    @Test
    void shouldThrowExceptionWhenLlmReturnsInvalidJson() {

        // Arrange
        LogInput input = new LogInput(
                "batch-125",
                "Compilation failed"
        );

        when(llmService.generate(anyString()))
                .thenReturn("This is not a valid JSON");

        // Act & Assert
        assertThrows(
                AgentException.class,
                () -> logAnalysisAgent.execute(
                        input,
                        new AgentContext()
                )
        );

        // Verify LLM interaction
        verify(llmService, times(1)).generate(anyString());
    }

    @Test
    void shouldThrowExceptionWhenLlmReturnsEmptyResponse() {

        // Arrange
        LogInput input = new LogInput(
                "batch-126",
                "Compilation failed"
        );

        when(llmService.generate(anyString()))
                .thenReturn("");

        // Act & Assert
        assertThrows(
                AgentException.class,
                () -> logAnalysisAgent.execute(
                        input,
                        new AgentContext()
                )
        );

        // Verify LLM interaction
        verify(llmService, times(1)).generate(anyString());
    }
}

