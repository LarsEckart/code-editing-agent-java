package com.larseckart.simple;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import java.io.IOException;
import java.util.Scanner;

public class SimpleApp {

  private final AnthropicClient client;

  private SimpleApp(String apiKey) {
    this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
  }

  public static void main(String[] args) throws IOException {
    String apiKey = System.getenv("ANTHROPIC_API_KEY");
    if (apiKey == null || apiKey.isBlank()) {
      System.err.println("Missing ANTHROPIC_API_KEY environment variable.");
      System.exit(1);
    }

    SimpleApp app = new SimpleApp(apiKey);
    Scanner scanner = new Scanner(System.in);
    System.out.println("Type 'exit' to quit.");

    while (true) {
      System.out.print("You: ");
      if (!scanner.hasNextLine()) {
        break;
      }
      String input = scanner.nextLine().trim();
      if (input.equalsIgnoreCase("exit")) {
        break;
      }

      try {
        String reply = app.askClaude(input);
        System.out.println("Claude: " + reply);
      } catch (Exception e) {
        System.err.println("Error talking to Claude: " + e.getMessage());
      }
    }
  }

  private String askClaude(String userMessage) {
    var params =
        MessageCreateParams.builder()
            .model(Model.CLAUDE_HAIKU_4_5)
            .maxTokens(400L)
            .addUserMessage(userMessage)
            .build();

    var response = client.messages().create(params);

    StringBuilder reply = new StringBuilder();
    response.content().forEach(block -> block.text().ifPresent(text -> reply.append(text.text())));

    return reply.toString().trim();
  }
}
