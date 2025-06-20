package com.larseckart.core.services;

import static org.slf4j.LoggerFactory.getLogger;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolUnion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.larseckart.ApiKey;
import com.larseckart.core.domain.ChatMessage;
import com.larseckart.core.domain.ConversationContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;

public class ConversationService {

  private static final Logger log = getLogger(ConversationService.class);

  private final ConversationContext context;
  private final AnthropicClient client;
  private final ToolRegistry toolRegistry;
  private final ObjectMapper objectMapper;

  public ConversationService(ConversationContext context, ApiKey apiKey) {
    log.info("Initializing ConversationService without tools");
    this.context = context;
    this.toolRegistry = null;
    this.objectMapper = new ObjectMapper();

    this.client = AnthropicOkHttpClient.builder()
        .apiKey(apiKey.getValue())
        .build();
    log.debug("ConversationService initialized successfully");
  }

  public ConversationService(ConversationContext context, ApiKey apiKey,
      ToolRegistry toolRegistry) {
    int toolCount = toolRegistry != null ? toolRegistry.getAllTools().size() : 0;
    log.info("Initializing ConversationService with {} tools", toolCount);
    this.context = context;
    this.toolRegistry = toolRegistry;
    this.objectMapper = new ObjectMapper();

    this.client = AnthropicOkHttpClient.builder()
        .apiKey(apiKey.getValue())
        .build();
    log.debug("ConversationService with tools initialized successfully");
  }

  public String sendMessage(String userInput) {
    log.info("Processing user message: {}",
        userInput.length() > 100 ? userInput.substring(0, 100) + "..." : userInput);
    context.addUserMessage(ChatMessage.user(userInput));

    try {
      var paramsBuilder = MessageCreateParams.builder()
          .model(Model.CLAUDE_3_5_HAIKU_LATEST)
          .maxTokens(4 * 1024L)
          // goals, constraints, and how to act
          .system(
              "You're from Germany, even when you reply in English, let some German words slip in. You're a huge fan of Estonia, mention or make references to Estonia whenever you can.");

      // Add tool definitions if available
      if (toolRegistry != null) {
        List<Map<String, Object>> toolDefinitions = toolRegistry.convertToClaudeFunctionDefinitions();
        if (!toolDefinitions.isEmpty()) {
          List<ToolUnion> tools = toolDefinitions.stream()
              .map(toolDef -> {
                String name = (String) toolDef.get("name");
                String description = (String) toolDef.get("description");
                Map<String, Object> inputSchema = (Map<String, Object>) toolDef.get("input_schema");

                Map<String, Object> properties = (Map<String, Object>) inputSchema.get(
                    "properties");
                JsonValue propertiesJson = JsonValue.from(properties);
                Tool.InputSchema schema = Tool.InputSchema.builder()
                    .properties(propertiesJson)
                    .build();

                Tool tool = Tool.builder()
                    .name(name)
                    .description(description)
                    .inputSchema(schema)
                    .build();

                return ToolUnion.ofTool(tool);
              })
              .toList();
          paramsBuilder.tools(tools);
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

      log.debug("Sending request to Claude API with {} history messages",
          context.getHistory().size());
      var response = client.messages().create(paramsBuilder.build());
      log.debug("Received response from Claude API, checking for tool use");

      // Check if response contains tool use
      if (hasToolUse(response)) {
        log.info("Response contains tool use, handling tools");
        return handleToolUse(response);
      } else {
        log.debug("Response contains text only, extracting content");
        return extractTextFromResponse(response);
      }
    } catch (Exception e) {
      log.error("Error processing message", e);
      throw new RuntimeException(e);
    }
  }

  private boolean hasToolUse(com.anthropic.models.messages.Message response) {
    return response.content().stream()
        .anyMatch(block -> block.toolUse().isPresent());
  }

  private String handleToolUse(com.anthropic.models.messages.Message response) {
    if (toolRegistry == null) {
      log.error("Tool use detected but no ToolRegistry available");
      throw new RuntimeException("Tool use detected but no ToolRegistry available");
    }

    try {
      // Execute all tools in the response
      StringBuilder allResults = new StringBuilder();
      log.debug("Starting tool execution for response");

      for (ContentBlock block : response.content()) {
        if (block.toolUse().isPresent()) {
          var toolUse = block.toolUse().get();
          String toolName = toolUse.name();
          JsonValue inputValue = toolUse._input();
          JsonNode parameters = inputValue.accept(new JsonValue.Visitor<JsonNode>() {
            @Override
            public JsonNode visitObject(Map<String, ? extends JsonValue> value) {
              return objectMapper.valueToTree(value);
            }

            @Override
            public JsonNode visitDefault() {
              // Return empty object node for non-object values
              return objectMapper.createObjectNode();
            }
          });

          // Execute the tool
          log.info("Executing tool: {}", toolName);
          log.debug("Tool parameters: {}", parameters);
          String result = toolRegistry.routeFunctionCall(toolName, parameters);
          log.debug("Tool {} executed successfully, result length: {}", toolName, result.length());
          allResults.append(result);

          // Add tool result to conversation context
          context.addAssistantMessage(
              ChatMessage.assistant("Tool " + toolName + " executed: " + result));
        }
      }

      // Send tool results back to Claude for final response
      log.debug("Sending tool results back to Claude for final response");
      return sendToolResultsToClaudeAndGetFinalResponse(response, allResults.toString());

    } catch (Exception e) {
      log.error("Tool execution failed", e);
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
          List<ToolUnion> tools = toolDefinitions.stream()
              .map(toolDef -> {
                String name = (String) toolDef.get("name");
                String description = (String) toolDef.get("description");
                Map<String, Object> inputSchema = (Map<String, Object>) toolDef.get("input_schema");

                Map<String, Object> properties = (Map<String, Object>) inputSchema.get(
                    "properties");
                JsonValue propertiesJson = JsonValue.from(properties);
                Tool.InputSchema schema = Tool.InputSchema.builder()
                    .properties(propertiesJson)
                    .build();

                Tool tool = Tool.builder()
                    .name(name)
                    .description(description)
                    .inputSchema(schema)
                    .build();

                return ToolUnion.ofTool(tool);
              })
              .collect(Collectors.toList());
          paramsBuilder.tools(tools);
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
