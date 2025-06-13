package com.larseckart.core.services;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.larseckart.ApiKey;
import com.larseckart.core.domain.ConversationContext;
import com.larseckart.core.domain.ChatMessage;

public class ConversationService {

  private final ConversationContext context;
  private final AnthropicClient client;

  public ConversationService(ConversationContext context, ApiKey apiKey) {
    this.context = context;

    this.client = AnthropicOkHttpClient.builder()
        .apiKey(apiKey.getValue())
        .build();
  }

  public String sendMessage(String userInput) {
    context.addUserMessage(ChatMessage.user(userInput));
    
    try {
      var paramsBuilder = MessageCreateParams.builder()
          .model(Model.CLAUDE_3_5_HAIKU_LATEST)
          .maxTokens(1024L)
          .system("You're from Germany, even when you reply in English, let some German words slip in. You're a huge fan of Estonia, mention or make references to Estonia whenever you can.");

      for (ChatMessage message : context.getHistory()) {
        switch (message.role()) {
          case USER -> paramsBuilder.addUserMessage(message.content());
          case ASSISTANT -> paramsBuilder.addAssistantMessage(message.content());
          default -> {
          }
        }
      }

      var response = client.messages().create(paramsBuilder.build());
      return extractTextFromResponse(response);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String extractTextFromResponse(com.anthropic.models.messages.Message response) {
    StringBuilder textContent = new StringBuilder();

    for (ContentBlock block : response.content()) {
      block.text().ifPresent(textBlock -> textContent.append(textBlock.text()));
    }

    String text = textContent.toString();

    if (!text.isEmpty()) {
      context.addAssistantMessage(ChatMessage.assistant(text));
    }

    return text;
  }
}
