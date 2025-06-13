package com.larseckart.core.services;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.larseckart.ApiKey;
import com.larseckart.core.domain.ConversationContext;
import java.util.logging.Logger;

public class ConversationService {

  private static final Logger logger = Logger.getLogger(ConversationService.class.getName());

  private final ConversationContext context;
  private final AnthropicClient client;

  public ConversationService(ConversationContext context, ApiKey apiKey) {
    this.context = context;

    this.client = AnthropicOkHttpClient.builder()
        .apiKey(apiKey.getValue())
        .build();
  }

  public String sendMessage(String userInput) {
    context.addUserMessage(userInput);
    
    try {
      var paramsBuilder = MessageCreateParams.builder()
          .model(Model.CLAUDE_3_5_HAIKU_LATEST)
          .maxTokens(1024L)
          .system("Be as brief as possible with your responses.");

      for (ConversationContext.Message message : context.getHistory()) {
        if ("user".equals(message.getRole())) {
          paramsBuilder.addUserMessage(message.getContent());
        } else if ("assistant".equals(message.getRole())) {
          paramsBuilder.addAssistantMessage(message.getContent());
        }
      }

      Message response = client.messages().create(paramsBuilder.build());
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
      context.addAssistantMessage(text);
    }

    return text;
  }
}
