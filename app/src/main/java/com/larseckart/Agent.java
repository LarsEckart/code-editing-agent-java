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
      System.out.println("\u001b[94mYou\u001b[0m: ");
      var userInput = supplier.get();
      if (userInput.isEmpty()) {
        quit = true;
      } else {
        client.send(userInput);
      }
    }
  }
}
