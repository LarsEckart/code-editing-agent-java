package com.larseckart.adapters.ai;

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
import com.larseckart.core.domain.ai.AIRequest;
import com.larseckart.core.domain.ai.AIResponse;
import com.larseckart.core.domain.ai.AITool;
import com.larseckart.core.domain.ai.AIToolUse;
import com.larseckart.core.ports.AIProvider;
import com.larseckart.core.services.ToolRegistry;
import com.larseckart.core.tools.EditFileTool;
import com.larseckart.core.tools.ListFilesTool;
import com.larseckart.core.tools.ReadFileTool;
import com.larseckart.core.tools.RunTestsTool;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class AnthropicProvider implements AIProvider {

  private static final Logger log = getLogger(AnthropicProvider.class);

  private final AnthropicClient client;
  private final ObjectMapper objectMapper;
  private final ToolRegistry toolRegistry;

  public AnthropicProvider(ApiKey apiKey) {
    this.client = AnthropicOkHttpClient.builder().apiKey(apiKey.getValue()).build();
    this.objectMapper = new ObjectMapper();

    // Initialize tools
    this.toolRegistry = new ToolRegistry();
    this.toolRegistry.registerTool(new ReadFileTool());
    this.toolRegistry.registerTool(new ListFilesTool());
    this.toolRegistry.registerTool(new EditFileTool());
    this.toolRegistry.registerTool(new RunTestsTool());

    log.debug("AnthropicProvider initialized with {} tools", toolRegistry.getAllTools().size());
  }

  @Override
  public AIResponse sendMessage(AIRequest request) {
    try {
      var paramsBuilder =
          MessageCreateParams.builder()
              .model(Model.CLAUDE_3_5_HAIKU_LATEST)
              .maxTokens((long) request.maxTokens())
              .system(request.systemPrompt());

      // Add internal tools
      List<AITool> aiTools = convertRegistryToAITools();
      if (!aiTools.isEmpty()) {
        List<ToolUnion> tools = convertToAnthropicTools(aiTools);
        paramsBuilder.tools(tools);
      }

      // Add messages
      for (ChatMessage message : request.messages()) {
        switch (message.role()) {
          case USER -> paramsBuilder.addUserMessage(message.content());
          case ASSISTANT -> paramsBuilder.addAssistantMessage(message.content());
          default -> {}
        }
      }

      log.debug("Sending request to Anthropic API");
      var response = client.messages().create(paramsBuilder.build());

      AIResponse aiResponse = convertToAIResponse(response);

      // Handle tool execution if needed
      if (aiResponse.hasToolUse()) {
        return handleToolExecution(request, aiResponse);
      }

      return aiResponse;

    } catch (Exception e) {
      log.error("Error calling Anthropic API", e);
      throw new RuntimeException("Anthropic API call failed", e);
    }
  }

  @Override
  public String getProviderName() {
    return "Anthropic Claude";
  }

  private List<ToolUnion> convertToAnthropicTools(List<AITool> aiTools) {
    return aiTools.stream().map(this::convertToAnthropicTool).toList();
  }

  private ToolUnion convertToAnthropicTool(AITool aiTool) {
    Map<String, Object> properties = (Map<String, Object>) aiTool.inputSchema().get("properties");
    JsonValue propertiesJson = JsonValue.from(properties);
    Tool.InputSchema schema = Tool.InputSchema.builder().properties(propertiesJson).build();

    Tool tool =
        Tool.builder()
            .name(aiTool.name())
            .description(aiTool.description())
            .inputSchema(schema)
            .build();

    return ToolUnion.ofTool(tool);
  }

  private AIResponse convertToAIResponse(com.anthropic.models.messages.Message response) {
    StringBuilder textContent = new StringBuilder();
    List<AIToolUse> toolUses = new ArrayList<>();
    boolean hasToolUse = false;

    for (ContentBlock block : response.content()) {
      // Extract text content
      block.text().ifPresent(textBlock -> textContent.append(textBlock.text()));

      // Extract tool uses
      if (block.toolUse().isPresent()) {
        hasToolUse = true;
        var toolUse = block.toolUse().get();
        String toolName = toolUse.name();
        JsonValue inputValue = toolUse._input();

        JsonNode parameters =
            inputValue.accept(
                new JsonValue.Visitor<>() {
                  @Override
                  public JsonNode visitObject(Map<String, ? extends JsonValue> value) {
                    return objectMapper.valueToTree(value);
                  }

                  @Override
                  public JsonNode visitDefault() {
                    return objectMapper.createObjectNode();
                  }
                });

        toolUses.add(new AIToolUse(toolName, parameters));
      }
    }

    return new AIResponse(textContent.toString(), toolUses, hasToolUse);
  }

  private List<AITool> convertRegistryToAITools() {
    List<Map<String, Object>> toolDefinitions = toolRegistry.convertToClaudeFunctionDefinitions();
    return toolDefinitions.stream()
        .map(
            toolDef ->
                new AITool(
                    (String) toolDef.get("name"),
                    (String) toolDef.get("description"),
                    (Map<String, Object>) toolDef.get("input_schema")))
        .toList();
  }

  private AIResponse handleToolExecution(AIRequest originalRequest, AIResponse toolResponse) {
    try {
      // Execute all tools in the response
      StringBuilder allResults = new StringBuilder();
      log.debug("Starting tool execution for response");

      for (AIToolUse toolUse : toolResponse.toolUses()) {
        String toolName = toolUse.toolName();

        // Execute the tool
        log.info("Executing tool: {}", toolName);
        log.debug("Tool parameters: {}", toolUse.parameters());
        String result = toolRegistry.routeFunctionCall(toolName, toolUse.parameters());
        log.debug("Tool {} executed successfully, result length: {}", toolName, result.length());
        allResults.append(result);
      }

      // Create a new request with tool results
      List<ChatMessage> updatedMessages = new ArrayList<>(originalRequest.messages());
      updatedMessages.add(ChatMessage.assistant(toolResponse.textContent()));
      updatedMessages.add(ChatMessage.user("Tool results: " + allResults.toString()));

      AIRequest followUpRequest =
          new AIRequest(
              updatedMessages,
              originalRequest.systemPrompt(),
              null, // Don't pass tools again
              originalRequest.maxTokens());

      // Get final response from AI
      log.debug("Sending tool results back to AI for final response");
      return sendMessage(followUpRequest);

    } catch (Exception e) {
      log.error("Tool execution failed", e);
      throw new RuntimeException("Tool execution failed", e);
    }
  }
}
