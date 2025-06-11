package com.larseckart;

import java.util.function.Supplier;

class Agent {

  private final Supplier<String> supplier;
  private final Client client;

  public Agent(Supplier<String> supplier, Client client) {
    this.supplier = supplier;
    this.client = client;
  }

  public void start() {
    System.out.println("Chat with Claude (use 'ctrl-c' to quit)");

    boolean quit = false;
    boolean readUserInput = true;

    while (!quit) {
      String userInput = "";
      if (readUserInput) {
        System.out.print("\u001b[94mYou\u001b[0m: ");
        userInput = supplier.get();
        if (userInput.isEmpty()) {
          quit = true;
          continue;
        }
      }

      // Get response from Claude
      String response = client.send(userInput);

      // Display response
      if (!response.isEmpty()) {
        System.out.print("\u001b[95mClaude\u001b[0m: ");
        System.out.println("\u001b[92m" + response + "\u001b[0m");
      }

      readUserInput = true;
    }
  }
}
