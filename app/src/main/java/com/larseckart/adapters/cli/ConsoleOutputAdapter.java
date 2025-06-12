package com.larseckart.adapters.cli;

import com.larseckart.core.ports.output.OutputPort;

public class ConsoleOutputAdapter implements OutputPort {

  @Override
  public void println(String message) {
    System.out.println(message);
  }

  @Override
  public void print(String message) {
    System.out.print(message);
  }
}