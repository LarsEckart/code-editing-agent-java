package com.larseckart.core.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConversationContext implements Serializable {

  private final List<String> history = new ArrayList<>();

  public void append(String input) {
    history.add(input);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < history.size(); i++) {
      String role = (i % 2 == 0) ? "user" : "system";
      builder.append(role).append(": ").append(history.get(i)).append("\n");
    }
    return builder.toString();
  }
}