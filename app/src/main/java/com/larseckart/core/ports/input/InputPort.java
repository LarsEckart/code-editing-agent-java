package com.larseckart.core.ports.input;

public interface InputPort {
  String readLine();
  String readLine(String prompt);
}