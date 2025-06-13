package com.larseckart.core.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConversationContext implements Serializable {

  public static class Message implements Serializable {
    private final String role;
    private final String content;

    public Message(String role, String content) {
      this.role = role;
      this.content = content;
    }

    public String getRole() {
      return role;
    }

    public String getContent() {
      return content;
    }
  }

  private final List<Message> history = new ArrayList<>();

  public void addUserMessage(String content) {
    history.add(new Message("user", content));
  }

  public void addAssistantMessage(String content) {
    history.add(new Message("assistant", content));
  }

  public List<Message> getHistory() {
    return new ArrayList<>(history);
  }

  public boolean isEmpty() {
    return history.isEmpty();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Message message : history) {
      builder.append(message.getRole().substring(0, 1).toUpperCase())
             .append(message.getRole().substring(1))
             .append(": ")
             .append(message.getContent())
             .append("\n");
    }
    return builder.toString();
  }
}