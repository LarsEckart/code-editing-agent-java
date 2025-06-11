package com.larseckart;

import java.util.Scanner;
import java.util.function.Supplier;

public class App {

  public static void main(String[] args) {
    // Setup input scanner
    Scanner scanner = new Scanner(System.in);
    Supplier<String> supplier = scanner::nextLine;
    
    // Create context and client
    Context context = new Context();
    Client client = new Client(context);
    
    // Create agent and start it
    new Agent(supplier, client).start();
  }
}
