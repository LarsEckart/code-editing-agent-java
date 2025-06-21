package com.larseckart.core.services;

import static org.slf4j.LoggerFactory.getLogger;

import com.larseckart.core.domain.ChatMessage;
import com.larseckart.core.domain.ConversationContext;
import com.larseckart.core.domain.ai.AIRequest;
import com.larseckart.core.domain.ai.AIResponse;
import com.larseckart.core.domain.ai.AITool;
import com.larseckart.core.domain.ai.AIToolUse;
import com.larseckart.core.ports.AIProvider;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class ConversationService {

  private static final Logger log = getLogger(ConversationService.class);

  private final ConversationContext context;
  private final AIProvider aiProvider;
  private final ToolRegistry toolRegistry;

  public ConversationService(ConversationContext context, AIProvider aiProvider) {
    log.info("Initializing ConversationService without tools");
    this.context = context;
    this.toolRegistry = null;
    this.aiProvider = aiProvider;
    log.debug("ConversationService initialized successfully");
  }

  public ConversationService(
      ConversationContext context, AIProvider aiProvider, ToolRegistry toolRegistry) {
    int toolCount = toolRegistry != null ? toolRegistry.getAllTools().size() : 0;
    log.info("Initializing ConversationService with {} tools", toolCount);
    this.context = context;
    this.toolRegistry = toolRegistry;
    this.aiProvider = aiProvider;
    log.debug("ConversationService with tools initialized successfully");
  }

  public String sendMessage(String userInput) {
    log.info(
        "Processing user message: {}",
        userInput.length() > 100 ? userInput.substring(0, 100) + "..." : userInput);
    context.addUserMessage(ChatMessage.user(userInput));

    try {
      // Convert tools if available
      List<AITool> tools = null;
      if (toolRegistry != null) {
        List<Map<String, Object>> toolDefinitions =
            toolRegistry.convertToClaudeFunctionDefinitions();
        if (!toolDefinitions.isEmpty()) {
          tools = toolDefinitions.stream()
              .map(toolDef -> new AITool(
                  (String) toolDef.get("name"),
                  (String) toolDef.get("description"),
                  (Map<String, Object>) toolDef.get("input_schema")))
              .toList();
        }
      }

      AIRequest request = new AIRequest(
          context.getHistory(),
          "You're from Germany, even when you reply in English, let some German words slip in. You're a huge fan of Estonia, mention or make references to Estonia whenever you can.",
          tools,
          4 * 1024);

      log.debug("Sending request to AI provider with {} history messages", context.getHistory().size());
      AIResponse response = aiProvider.sendMessage(request);
      log.debug("Received response from AI provider, checking for tool use");

      // Check if response contains tool use
      if (response.hasToolUse()) {
        log.info("Response contains tool use, handling tools");
        return handleToolUse(response);
      } else {
        log.debug("Response contains text only, extracting content");
        String text = response.textContent();
        if (!text.isEmpty()) {
          context.addAssistantMessage(ChatMessage.assistant(text));
        }
        return text;
      }
    } catch (Exception e) {
      log.error("Error processing message", e);
      throw new RuntimeException(e);
    }
  }

  private String handleToolUse(AIResponse response) {
    if (toolRegistry == null) {
      log.error("Tool use detected but no ToolRegistry available");
      throw new RuntimeException("Tool use detected but no ToolRegistry available");
    }

    try {
      // Execute all tools in the response
      StringBuilder allResults = new StringBuilder();
      log.debug("Starting tool execution for response");

      for (AIToolUse toolUse : response.toolUses()) {
        String toolName = toolUse.toolName();
        
        // Execute the tool
        log.info("Executing tool: {}", toolName);
        log.debug("Tool parameters: {}", toolUse.parameters());
        String result = toolRegistry.routeFunctionCall(toolName, toolUse.parameters());
        log.debug("Tool {} executed successfully, result length: {}", toolName, result.length());
        allResults.append(result);

        // Add tool result to conversation context
        context.addAssistantMessage(
            ChatMessage.assistant("Tool " + toolName + " executed: " + result));
      }

      // Send tool results back to AI for final response
      log.debug("Sending tool results back to AI for final response");
      return sendToolResultsToAIAndGetFinalResponse(allResults.toString());

    } catch (Exception e) {
      log.error("Tool execution failed", e);
      throw new RuntimeException("Tool execution failed", e);
    }
  }

  private String sendToolResultsToAIAndGetFinalResponse(String toolResults) {
    try {
      // Convert tools if available
      List<AITool> tools = null;
      if (toolRegistry != null) {
        List<Map<String, Object>> toolDefinitions =
            toolRegistry.convertToClaudeFunctionDefinitions();
        if (!toolDefinitions.isEmpty()) {
          tools = toolDefinitions.stream()
              .map(toolDef -> new AITool(
                  (String) toolDef.get("name"),
                  (String) toolDef.get("description"),
                  (Map<String, Object>) toolDef.get("input_schema")))
              .toList();
        }
      }

      // Add tool results as user message to context
      context.addUserMessage(ChatMessage.user("Tool results: " + toolResults));

      AIRequest request = new AIRequest(
          context.getHistory(),
          "You're from Germany, even when you reply in English, let some German words slip in. You're a huge fan of Estonia, mention or make references to Estonia whenever you can.",
          tools,
          1024);

      AIResponse finalResponse = aiProvider.sendMessage(request);
      String text = finalResponse.textContent();
      
      if (!text.isEmpty()) {
        context.addAssistantMessage(ChatMessage.assistant(text));
      }
      
      return text;

    } catch (Exception e) {
      throw new RuntimeException("Failed to get final response after tool execution", e);
    }
  }
}
