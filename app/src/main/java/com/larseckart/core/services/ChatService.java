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
    outputPort.showWelcomeMessage();

    boolean quit = false;

    while (!quit) {
      outputPort.promptForUserInput();
      String userInput = inputPort.readLine();
      if (userInput.isEmpty()) {
        quit = true;
        continue;
      }

      String response = conversationService.sendMessage(userInput);

      if (!response.isEmpty()) {
        outputPort.displayAssistantResponse(response);
      }
    }
  }
}