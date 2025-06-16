package com.larseckart.core.services;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ToolUnion;
import com.anthropic.models.messages.ToolUseBlock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.larseckart.ApiKey;
import com.larseckart.core.domain.ChatMessage;
import com.larseckart.core.domain.ConversationContext;

import java.util.List;
import java.util.Map;

public class ConversationService {

  private final ConversationContext context;
  private final AnthropicClient client;
  private final ToolRegistry toolRegistry;
  private final ObjectMapper objectMapper;

  public ConversationService(ConversationContext context, ApiKey apiKey) {
    this.context = context;
    this.toolRegistry = null;
    this.objectMapper = new ObjectMapper();

    this.client = AnthropicOkHttpClient.builder()
        .apiKey(apiKey.getValue())
        .build();
  }

  public ConversationService(ConversationContext context, ApiKey apiKey, ToolRegistry toolRegistry) {
    this.context = context;
    this.toolRegistry = toolRegistry;
    this.objectMapper = new ObjectMapper();

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
          // goals, constraints, and how to act
          .system(
              "You're from Germany, even when you reply in English, let some German words slip in. You're a huge fan of Estonia, mention or make references to Estonia whenever you can.");

      // Add tool definitions if available
      if (toolRegistry != null) {
        List<Map<String, Object>> toolDefinitions = toolRegistry.convertToClaudeFunctionDefinitions();
        if (!toolDefinitions.isEmpty()) {
          paramsBuilder.tools(toolDefinitions);
        }
      }

      for (ChatMessage message : context.getHistory()) {
        switch (message.role()) {
          case USER -> paramsBuilder.addUserMessage(message.content());
          case ASSISTANT -> paramsBuilder.addAssistantMessage(message.content());
          default -> {
          }
        }
      }

      var response = client.messages().create(paramsBuilder.build());
      
      // Check if response contains tool use
      if (hasToolUse(response)) {
        return handleToolUse(response);
      } else {
        return extractTextFromResponse(response);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private boolean hasToolUse(com.anthropic.models.messages.Message response) {
    return response.content().stream()
        .anyMatch(block -> block.toolUse().isPresent());
  }

  private String handleToolUse(com.anthropic.models.messages.Message response) {
    if (toolRegistry == null) {
      throw new RuntimeException("Tool use detected but no ToolRegistry available");
    }

    try {
      // Execute all tools in the response
      StringBuilder allResults = new StringBuilder();
      
      for (ContentBlock block : response.content()) {
        if (block.toolUse().isPresent()) {
          var toolUse = block.toolUse().get();
          String toolName = toolUse.name();
          JsonNode parameters = toolUse.input();
          
          // Execute the tool
          String result = toolRegistry.routeFunctionCall(toolName, parameters);
          allResults.append(result);
          
          // Add tool result to conversation context
          context.addAssistantMessage(ChatMessage.assistant("Tool " + toolName + " executed: " + result));
        }
      }
      
      // Send tool results back to Claude for final response
      return sendToolResultsToClaudeAndGetFinalResponse(response, allResults.toString());
      
    } catch (Exception e) {
      throw new RuntimeException("Tool execution failed", e);
    }
  }

  private String sendToolResultsToClaudeAndGetFinalResponse(
      com.anthropic.models.messages.Message toolUseResponse, 
      String toolResults) {
    
    try {
      // Create a new message with tool results
      var paramsBuilder = MessageCreateParams.builder()
          .model(Model.CLAUDE_3_5_HAIKU_LATEST)
          .maxTokens(1024L)
          .system(
              "You're from Germany, even when you reply in English, let some German words slip in. You're a huge fan of Estonia, mention or make references to Estonia whenever you can.");

      // Add tool definitions if available
      if (toolRegistry != null) {
        List<Map<String, Object>> toolDefinitions = toolRegistry.convertToClaudeFunctionDefinitions();
        if (!toolDefinitions.isEmpty()) {
          paramsBuilder.tools(toolDefinitions);
        }
      }

      // Add conversation history
      for (ChatMessage message : context.getHistory()) {
        switch (message.role()) {
          case USER -> paramsBuilder.addUserMessage(message.content());
          case ASSISTANT -> paramsBuilder.addAssistantMessage(message.content());
          default -> {
          }
        }
      }

      // Add tool results as user message
      paramsBuilder.addUserMessage("Tool results: " + toolResults);

      var finalResponse = client.messages().create(paramsBuilder.build());
      return extractTextFromResponse(finalResponse);
      
    } catch (Exception e) {
      throw new RuntimeException("Failed to get final response after tool execution", e);
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
