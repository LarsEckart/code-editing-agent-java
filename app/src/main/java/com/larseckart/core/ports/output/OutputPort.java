package com.larseckart.core.ports.output;

public interface OutputPort {
  void showWelcomeMessage();
  void promptForUserInput();
  void displayAssistantResponse(String response);
}
