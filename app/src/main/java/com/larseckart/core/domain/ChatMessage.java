package com.larseckart.core.domain;

import java.io.Serializable;

public record ChatMessage(Role role, String content) implements Serializable {

  public static ChatMessage user(String content) {
    return new ChatMessage(Role.USER, content);
  }

  public static ChatMessage assistant(String content) {
    return new ChatMessage(Role.ASSISTANT, content);
  }
}
