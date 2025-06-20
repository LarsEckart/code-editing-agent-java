package com.larseckart.adapters.cli;

import com.larseckart.core.ports.input.InputPort;
import java.util.Scanner;

public class ConsoleInputAdapter implements InputPort {

  private final Scanner scanner;

  public ConsoleInputAdapter() {
    this.scanner = new Scanner(System.in);
  }

  @Override
  public String readLine() {
    return scanner.nextLine();
  }
}
