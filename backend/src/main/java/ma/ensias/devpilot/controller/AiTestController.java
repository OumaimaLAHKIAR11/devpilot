
package ma.ensias.devpilot.controller;

import ma.ensias.devpilot.ai.LlmService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiTestController {

    private final LlmService llmService;

    public AiTestController(LlmService llmService) {
        this.llmService = llmService;
    }

    @GetMapping("/api/ai/test")
    public String testAi() {
        return llmService.generate(
                "Réponds uniquement : Bonjour DevPilot"
        );
    }
}
