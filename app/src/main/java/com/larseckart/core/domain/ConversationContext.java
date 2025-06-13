package com.larseckart.core.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConversationContext implements Serializable {

  private final List<ChatMessage> history = new ArrayList<>();

  public void addUserMessage(ChatMessage user) {
    history.add(user);
  }

  public void addAssistantMessage(ChatMessage assistant) {
    history.add(assistant);
  }

  public List<ChatMessage> getHistory() {
    return List.copyOf(history);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (ChatMessage message : history) {
      String roleName = message.role().name().toLowerCase();
      builder.append(roleName.substring(0, 1).toUpperCase())
             .append(roleName.substring(1))
             .append(": ")
             .append(message.content())
             .append("\n");
    }
    return builder.toString();
  }
}
