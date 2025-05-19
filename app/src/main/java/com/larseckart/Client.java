package com.larseckart;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.larseckart.tool.ToolDefinition;

class Client {

  private static final Logger logger = Logger.getLogger(Client.class.getName());
  private final Context context;
  private final String apiUrl;
  private final List<ToolDefinition> tools;
  public HttpClient client;
  public ObjectMapper objectMapper;

  public Client(Context context) {
    this(context, null, new ArrayList<>());
  }

  public Client(Context context, String apiUrl) {
    this(context, apiUrl, new ArrayList<>());
  }

  public Client(Context context, String apiUrl, List<ToolDefinition> tools) {
    this.context = context;
    this.tools = tools;
    this.client = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
    if (apiUrl != null) {
      this.apiUrl = apiUrl;
    } else {
      String apiKey = System.getenv("ANTHROPIC_API_KEY");
      if (apiKey == null || apiKey.isEmpty()) {
        logger.warning("No API key found. Set ANTHROPIC_API_KEY environment variable.");
        apiKey = "demo_key"; // Placeholder for testing without a key
      }
      this.apiUrl = "https://api.anthropic.com/v1/messages";
    }
  }

  public Message send(String userInput) {
    context.append(userInput);
    try {
      String jsonPayload = buildJsonPayload(userInput);
      HttpRequest request = buildHttpRequest(apiUrl, jsonPayload);
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return handleResponse(response.body());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String buildJsonPayload(String userInput) throws JsonProcessingException {
    ObjectNode rootNode = objectMapper.createObjectNode();
    rootNode.put("model", "claude-3-7-sonnet-20240620");
    rootNode.put("max_tokens", 1024);
    
    // Messages array
    ArrayNode messagesArray = rootNode.putArray("messages");
    
    // Add user message
    ObjectNode userMessage = messagesArray.addObject();
    userMessage.put("role", "user");
    ArrayNode userContent = userMessage.putArray("content");
    ObjectNode textPart = userContent.addObject();
    textPart.put("type", "text");
    textPart.put("text", userInput);
    
    // Add tools if available
    if (!tools.isEmpty()) {
      ArrayNode toolsArray = rootNode.putArray("tools");
      
      for (ToolDefinition tool : tools) {
        ObjectNode toolObj = toolsArray.addObject();
        ObjectNode toolDef = toolObj.putObject("function");
        toolDef.put("name", tool.getName());
        toolDef.put("description", tool.getDescription());
        toolDef.set("parameters", tool.getInputSchema());
      }
    }
    
    return objectMapper.writeValueAsString(rootNode);
  }

  private HttpRequest buildHttpRequest(String url, String jsonPayload) {
    String apiKey = System.getenv("ANTHROPIC_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      apiKey = "demo_key"; // Placeholder for testing
    }
    
    return HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .header("x-api-key", apiKey)
        .header("anthropic-version", "2023-06-01")
        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
        .build();
  }

  private Message handleResponse(String responseBody) throws Exception {
    JsonNode responseNode = objectMapper.readTree(responseBody);
    
    // Check for error response
    if (responseNode.has("error")) {
      JsonNode errorNode = responseNode.get("error");
      String errorType = errorNode.path("type").asText("unknown_error");
      String errorMessage = errorNode.path("message").asText("Unknown error occurred");
      logger.warning("API error: " + errorType + ": " + errorMessage);
      
      Message errorResponse = new Message();
      errorResponse.setText("Error: " + errorMessage);
      return errorResponse;
    }
    
    // Handle successful response
    Message message = new Message();
    
    // Process content blocks
    JsonNode content = responseNode.path("content");
    if (content.isArray()) {
      StringBuilder textContent = new StringBuilder();
      List<ToolCall> toolCalls = new ArrayList<>();
      
      for (JsonNode block : content) {
        String type = block.path("type").asText();
        
        if ("text".equals(type)) {
          textContent.append(block.path("text").asText());
        } else if ("tool_use".equals(type)) {
          String id = block.path("id").asText();
          String toolName = block.path("name").asText();
          JsonNode toolInput = block.path("input");
          
          ToolCall toolCall = new ToolCall(id, toolName, toolInput);
          toolCalls.add(toolCall);
        }
      }
      
      message.setText(textContent.toString());
      message.setToolCalls(toolCalls);
    }
    
    // Save the assistant's text response to context
    if (!message.getText().isEmpty()) {
      context.append(message.getText());
    }
    
    return message;
  }
  
  public static class Message {
    private String text = "";
    private List<ToolCall> toolCalls = new ArrayList<>();
    
    public String getText() {
      return text;
    }
    
    public void setText(String text) {
      this.text = text;
    }
    
    public List<ToolCall> getToolCalls() {
      return toolCalls;
    }
    
    public void setToolCalls(List<ToolCall> toolCalls) {
      this.toolCalls = toolCalls;
    }
    
    public boolean hasToolCalls() {
      return !toolCalls.isEmpty();
    }
  }
  
  public static class ToolCall {
    private final String id;
    private final String name;
    private final JsonNode input;
    
    public ToolCall(String id, String name, JsonNode input) {
      this.id = id;
      this.name = name;
      this.input = input;
    }
    
    public String getId() {
      return id;
    }
    
    public String getName() {
      return name;
    }
    
    public JsonNode getInput() {
      return input;
    }
  }
}
