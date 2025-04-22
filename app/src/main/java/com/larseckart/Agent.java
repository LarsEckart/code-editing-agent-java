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
    System.out.println("Chat with Gemini (use 'ctrl-c' to quit)");

    boolean quit = false;
    while (!quit) {
      System.out.print("\u001b[94mYou\u001b[0m: ");
      var userInput = supplier.get();
      if (userInput.isEmpty()) {
        quit = true;
      } else {
        String answer = client.send(userInput);
        System.out.print("\u001b[95mGemini\u001b[0m: "); // Magenta for Gemini
        if (answer != null) {
          System.out.println("\u001b[92m" + answer + "\u001b[0m"); // Green for answer
        } else {
          System.out.println("\u001b[91m[No answer found]\u001b[0m"); // Red for missing answer
        }
      }
    }
  }
}
