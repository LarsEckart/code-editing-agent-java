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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class AnthropicProvider implements AIProvider {

  private static final Logger log = getLogger(AnthropicProvider.class);

  private final AnthropicClient client;
  private final ObjectMapper objectMapper;

  public AnthropicProvider(ApiKey apiKey) {
    this.client = AnthropicOkHttpClient.builder().apiKey(apiKey.getValue()).build();
    this.objectMapper = new ObjectMapper();
    log.debug("AnthropicProvider initialized");
  }

  @Override
  public AIResponse sendMessage(AIRequest request) {
    try {
      var paramsBuilder =
          MessageCreateParams.builder()
              .model(Model.CLAUDE_3_5_HAIKU_LATEST)
              .maxTokens((long) request.maxTokens())
              .system(request.systemPrompt());

      // Add tools if present
      if (request.tools() != null && !request.tools().isEmpty()) {
        List<ToolUnion> tools = convertToAnthropicTools(request.tools());
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
      
      return convertToAIResponse(response);

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
    return aiTools.stream()
        .map(this::convertToAnthropicTool)
        .toList();
  }

  private ToolUnion convertToAnthropicTool(AITool aiTool) {
    Map<String, Object> properties = (Map<String, Object>) aiTool.inputSchema().get("properties");
    JsonValue propertiesJson = JsonValue.from(properties);
    Tool.InputSchema schema = Tool.InputSchema.builder().properties(propertiesJson).build();

    Tool tool = Tool.builder()
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
        
        JsonNode parameters = inputValue.accept(
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
}
