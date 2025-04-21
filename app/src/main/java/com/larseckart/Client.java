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
  public HttpClient client;
  public ObjectMapper objectMapper;

  public Client(Context context) {
    this.context = context;
    this.client = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
  }

  public void send(String userInput) {
    context.append(userInput);
    String apiKey = System.getenv("GEMINI_API_KEY");
    String url =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
        + apiKey;

    try {
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

      String jsonPayload = objectMapper.writeValueAsString(rootNode);

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
          .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      String responseBody = response.body();

      JsonNode responseRootNode = objectMapper.readTree(responseBody);
      JsonNode candidatesNode = responseRootNode.path("candidates");
      if (candidatesNode.isArray() && !candidatesNode.isEmpty()) {
        JsonNode contentNode = candidatesNode.get(0).path("content").path("parts");
        if (contentNode.isArray() && !contentNode.isEmpty()) {
          String answer = contentNode.get(0).path("text").asText();
          System.out.println("Answer: " + answer);
          context.append(answer);
        } else {
          System.out.println("No answer found in response.");
        }
      } else {
        System.out.println("No candidates found in response.");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
