package com.larseckart.adapters.cli;

import com.larseckart.core.ports.output.OutputPort;

public class ConsoleOutputAdapter implements OutputPort {

  @Override
  public void showWelcomeMessage() {
    System.out.println("Chat with a LLM (use 'ctrl-c' to quit or press Enter on empty line)");
  }

  @Override
  public void promptForUserInput() {
    System.out.print("\u001b[94mYou\u001b[0m: ");
  }

  @Override
  public void displayAssistantResponse(String response) {
    System.out.print("\u001b[95mLarsGPT\u001b[0m: ");
    System.out.println("\u001b[92m" + response + "\u001b[0m");
  }
}
