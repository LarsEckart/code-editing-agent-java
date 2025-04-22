package com.larseckart;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

class Client {

  private final Context context;
  private final String apiUrl;
  public HttpClient client;
  public ObjectMapper objectMapper;

  public Client(Context context) {
    this(context, null);
  }

  public Client(Context context, String apiUrl) {
    this.context = context;
    this.client = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
    if (apiUrl != null) {
      this.apiUrl = apiUrl;
    } else {
      String apiKey = System.getenv("GEMINI_API_KEY");
      this.apiUrl =
          "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
          + apiKey;
    }
  }

  public String send(String userInput) {
    context.append(userInput);
    try {
      String jsonPayload = buildJsonPayload();
      HttpRequest request = buildHttpRequest(apiUrl, jsonPayload);
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return handleResponse(response.body());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String buildJsonPayload() throws Exception {
    ObjectNode rootNode = objectMapper.createObjectNode();
    ArrayNode contentsArray = rootNode.putArray("contents");
    ObjectNode contentObject = contentsArray.addObject();
    ArrayNode parts = contentObject.putArray("parts");
    List<String> contextHistory = context.getHistory();
    for (int i = 0, contextHistorySize = contextHistory.size(); i < contextHistorySize; i++) {
      String history = contextHistory.get(i);
      ObjectNode part = parts.addObject();
      part.put("text", history);
      if (i % 2 != 0) {
        part.put("thought", true);
      }
    }
    return objectMapper.writeValueAsString(rootNode);
  }

  private HttpRequest buildHttpRequest(String url, String jsonPayload) {
    return HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
        .build();
  }

  private String handleResponse(String responseBody) throws Exception {
    JsonNode responseRootNode = objectMapper.readTree(responseBody);
    JsonNode candidatesNode = responseRootNode.path("candidates");
    if (candidatesNode.isArray() && !candidatesNode.isEmpty()) {
      JsonNode contentNode = candidatesNode.get(0).path("content").path("parts");
      if (contentNode.isArray() && !contentNode.isEmpty()) {
        String answer = contentNode.get(0).path("text").asText();
        context.append(answer);
        return answer;
      }
    }
    return "Something went wrong. Please try again.";
  }
}
