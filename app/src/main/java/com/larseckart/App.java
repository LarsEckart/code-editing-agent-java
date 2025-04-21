package com.larseckart;

import java.util.Scanner;
import java.util.function.Supplier;

public class App {

  public String getGreeting() {
    return "Hello World!";
  }

  public static void main(String[] args) {

    Scanner scanner = new Scanner(System.in);

    Supplier<String> supplier = scanner::nextLine;

    new Agent(supplier, new Client(new Context())).start();

  }
}
