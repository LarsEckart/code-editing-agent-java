package com.larseckart;

import java.util.logging.Logger;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;

class Client {

    private static final Logger logger = Logger.getLogger(Client.class.getName());

    private final Context context;
    private final AnthropicClient client;

    public Client(Context context) {
        this.context = context;

        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warning("No API key found. Set ANTHROPIC_API_KEY environment variable.");
            throw new IllegalArgumentException("API key is required. Set ANTHROPIC_API_KEY environment variable.");
        }

        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    public String send(String userInput) {
        context.append(userInput);
        try {
            // Send the entire conversation history, not just the current input
            String conversationHistory = context.toString();

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_3_5_HAIKU_LATEST)
                    .maxTokens(1024L)
                    .addUserMessage(conversationHistory)
                    .build();

            Message response = client.messages().create(params);
            return extractTextFromResponse(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extractTextFromResponse(Message response) {
        StringBuilder textContent = new StringBuilder();

        for (ContentBlock block : response.content()) {
            block.text().ifPresent(textBlock -> textContent.append(textBlock.text()));
        }

        String text = textContent.toString();

        if (!text.isEmpty()) {
            context.append(text);
        }

        return text;
    }

}
