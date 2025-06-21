package com.larseckart.adapters.ai;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.larseckart.ApiKey;
import com.larseckart.core.domain.ChatMessage;
import com.larseckart.core.domain.ai.AIRequest;
import com.larseckart.core.domain.ai.AIResponse;
import com.larseckart.core.domain.ai.AIToolUse;
import com.larseckart.core.ports.AIProvider;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

public class GeminiProvider implements AIProvider {

  private static final Logger log = getLogger(GeminiProvider.class);
  private static final String DEFAULT_MODEL = "gemini-2.0-flash-001";

  private final Client client;
  private final ObjectMapper objectMapper;

  public GeminiProvider(ApiKey apiKey) {
    this.client = Client.builder().apiKey(apiKey.getValue()).build();
    this.objectMapper = new ObjectMapper();
    log.debug("GeminiProvider initialized");
  }

  @Override
  public AIResponse sendMessage(AIRequest request) {
    try {
      // Convert chat messages to Gemini Content format
      List<Content> messages = convertToGeminiMessages(request.messages());

      // Build configuration
      GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder();

      if (request.maxTokens() > 0) {
        configBuilder.maxOutputTokens(request.maxTokens());
      }

      // Set system instruction if provided
      if (request.systemPrompt() != null && !request.systemPrompt().isEmpty()) {
        Content systemInstruction = Content.fromParts(Part.fromText(request.systemPrompt()));
        configBuilder.systemInstruction(systemInstruction);
      }

      // Add tools if present (Gemini tools work differently, skipping for now)
      // TODO: Implement function calling support

      GenerateContentConfig config = configBuilder.build();

      log.debug("Sending request to Gemini API");

      // For simplicity, use the last message as the prompt
      // In a full implementation, we'd need to handle conversation history properly
      Content prompt =
          messages.isEmpty()
              ? Content.fromParts(Part.fromText("Hello"))
              : messages.get(messages.size() - 1);

      GenerateContentResponse response =
          client.models.generateContent(DEFAULT_MODEL, prompt, config);

      return convertToAIResponse(response);

    } catch (Exception e) {
      log.error("Error calling Gemini API", e);
      throw new RuntimeException("Gemini API call failed", e);
    }
  }

  @Override
  public String getProviderName() {
    return "Google Gemini";
  }

  private List<Content> convertToGeminiMessages(List<ChatMessage> messages) {
    List<Content> geminiMessages = new ArrayList<>();

    for (ChatMessage message : messages) {
      Content content = Content.fromParts(Part.fromText(message.content()));
      geminiMessages.add(content);
    }

    return geminiMessages;
  }

  private AIResponse convertToAIResponse(GenerateContentResponse response) {
    // Extract text content
    String textContent = response.text();

    // For now, Gemini tool use is not implemented
    List<AIToolUse> toolUses = new ArrayList<>();
    boolean hasToolUse = false;

    return new AIResponse(textContent, toolUses, hasToolUse);
  }
}
