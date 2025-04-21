package com.larseckart;

import java.util.ArrayList;
import java.util.List;

class Context {

  private final List<String> history = new ArrayList<>();

  public void append(String userInput) {
    history.add(userInput);
  }

  public List<String> getHistory() {
    return List.copyOf(history);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < history.size(); i++) {
      builder.append(i).append(": ").append(history.get(i)).append("\n");
    }
    return builder.toString();
  }
}
