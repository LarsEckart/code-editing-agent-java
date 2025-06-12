package com.larseckart.core.services;

import com.larseckart.core.ports.input.InputPort;
import com.larseckart.core.ports.output.OutputPort;

public class ChatService {

  private final InputPort inputPort;
  private final OutputPort outputPort;
  private final ConversationService conversationService;

  public ChatService(InputPort inputPort, OutputPort outputPort, ConversationService conversationService) {
    this.inputPort = inputPort;
    this.outputPort = outputPort;
    this.conversationService = conversationService;
  }

  public void startChat() {
    outputPort.println("Chat with Claude (use 'ctrl-c' to quit)");

    boolean quit = false;
    boolean readUserInput = true;

    while (!quit) {
      String userInput = "";
      if (readUserInput) {
        outputPort.print("\u001b[94mYou\u001b[0m: ");
        userInput = inputPort.readLine();
        if (userInput.isEmpty()) {
          quit = true;
          continue;
        }
      }

      String response = conversationService.sendMessage(userInput);

      if (!response.isEmpty()) {
        outputPort.print("\u001b[95mClaude\u001b[0m: ");
        outputPort.println("\u001b[92m" + response + "\u001b[0m");
      }

      readUserInput = true;
    }
  }
}