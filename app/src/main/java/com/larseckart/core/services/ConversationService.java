package com.larseckart.core.services;

import static org.slf4j.LoggerFactory.getLogger;

import com.larseckart.core.domain.ChatMessage;
import com.larseckart.core.domain.ConversationContext;
import com.larseckart.core.domain.ai.AIRequest;
import com.larseckart.core.domain.ai.AIResponse;
import com.larseckart.core.ports.AIProvider;
import org.slf4j.Logger;

public class ConversationService {

  private static final Logger log = getLogger(ConversationService.class);

  private final ConversationContext context;
  private final AIProvider aiProvider;

  public ConversationService(ConversationContext context, AIProvider aiProvider) {
    log.info("Initializing ConversationService");
    this.context = context;
    this.aiProvider = aiProvider;
    log.debug("ConversationService initialized successfully");
  }

  public String sendMessage(String userInput) {
    log.info(
        "Processing user message: {}",
        userInput.length() > 100 ? userInput.substring(0, 100) + "..." : userInput);
    context.addUserMessage(ChatMessage.user(userInput));

    try {
      AIRequest request =
          new AIRequest(
              context.getHistory(),
              "You're from Germany, even when you reply in English, let some German words slip in. You're a huge fan of Estonia, mention or make references to Estonia whenever you can.",
              null, // AI providers handle their own tools
              4 * 1024);

      log.debug(
          "Sending request to AI provider with {} history messages", context.getHistory().size());
      AIResponse response = aiProvider.sendMessage(request);
      log.debug("Received response from AI provider");

      // Add response to context
      String text = response.textContent();
      if (!text.isEmpty()) {
        context.addAssistantMessage(ChatMessage.assistant(text));
      }
      return text;
    } catch (Exception e) {
      log.error("Error processing message", e);
      throw new RuntimeException(e);
    }
  }
}
