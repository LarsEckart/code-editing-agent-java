package com.larseckart;

import java.util.Scanner;
import java.util.function.Supplier;

public class App {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    Supplier<String> supplier = scanner::nextLine;

    Client client = new Client(new Context(), ApiKey.fromEnvironment("code_editing_agent_api_key"));

    new Agent(supplier, client).start();
  }
}
