
package ma.ensias.devpilot.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiTestController {

    private final ChatClient chatClient;

    public AiTestController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/api/ai/test")
    public String testAi() {
        return chatClient.prompt()
                .user("Réponds uniquement : Bonjour DevPilot")
                .call()
                .content();
    }
}
