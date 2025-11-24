package com.larseckart.better;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BetterApp {

  private final AnthropicClient client;
  private final List<ChatMessage> history;

  private BetterApp(String apiKey) {
    this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    this.history = new ArrayList<>();
  }

  public static void main(String[] args) {
    String apiKey = System.getenv("ANTHROPIC_API_KEY");
    if (apiKey == null || apiKey.isBlank()) {
      System.err.println("Missing ANTHROPIC_API_KEY environment variable.");
      System.exit(1);
    }

    BetterApp app = new BetterApp(apiKey);
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

      app.history.add(ChatMessage.user(input));

      try {
        String reply = app.askClaude();
        System.out.println("Claude: " + reply);
        app.history.add(ChatMessage.assistant(reply));
      } catch (Exception e) {
        System.err.println("Error talking to Claude: " + e.getMessage());
      }
    }
  }

  private String askClaude() {
    MessageCreateParams.Builder paramsBuilder =
        MessageCreateParams.builder()
            .model(Model.CLAUDE_HAIKU_4_5)
            .maxTokens(400L);

    // send full history for conversation continuity
    for (ChatMessage message : history) {
      switch (message.role) {
        case USER -> paramsBuilder.addUserMessage(message.content);
        case ASSISTANT -> paramsBuilder.addAssistantMessage(message.content);
        default -> {}
      }
    }

    var response = client.messages().create(paramsBuilder.build());

    StringBuilder reply = new StringBuilder();
    response.content().forEach(block -> block.text().ifPresent(text -> reply.append(text.text())));
    return reply.toString().trim();
  }

  private enum Role {
    USER,
    ASSISTANT
  }

  private record ChatMessage(Role role, String content) {
    static ChatMessage user(String content) {
      return new ChatMessage(Role.USER, content);
    }

    static ChatMessage assistant(String content) {
      return new ChatMessage(Role.ASSISTANT, content);
    }
  }
}
