package com.larseckart.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ConversationContextTest {

  private ConversationContext context;

  @BeforeEach
  void setUp() {
    context = new ConversationContext();
  }

  @Test
  void test_empty_context() {
    List<ChatMessage> history = context.getHistory();
    assertTrue(history.isEmpty());
  }

  @Test
  void test_add_user_message() {
    ChatMessage userMessage = ChatMessage.user("Hello");
    context.addUserMessage(userMessage);

    List<ChatMessage> history = context.getHistory();
    assertEquals(1, history.size());
    assertEquals(userMessage, history.get(0));
  }

  @Test
  void test_add_assistant_message() {
    ChatMessage assistantMessage = ChatMessage.assistant("Hi there!");
    context.addAssistantMessage(assistantMessage);

    List<ChatMessage> history = context.getHistory();
    assertEquals(1, history.size());
    assertEquals(assistantMessage, history.get(0));
  }

  @Test
  void test_add_multiple_messages() {
    ChatMessage userMessage1 = ChatMessage.user("Hello");
    ChatMessage assistantMessage1 = ChatMessage.assistant("Hi there!");
    ChatMessage userMessage2 = ChatMessage.user("How are you?");
    ChatMessage assistantMessage2 = ChatMessage.assistant("I'm doing well!");

    context.addUserMessage(userMessage1);
    context.addAssistantMessage(assistantMessage1);
    context.addUserMessage(userMessage2);
    context.addAssistantMessage(assistantMessage2);

    List<ChatMessage> history = context.getHistory();
    assertEquals(4, history.size());
    assertEquals(userMessage1, history.get(0));
    assertEquals(assistantMessage1, history.get(1));
    assertEquals(userMessage2, history.get(2));
    assertEquals(assistantMessage2, history.get(3));
  }

  @Test
  void test_get_history_returns_immutable_list() {
    ChatMessage userMessage = ChatMessage.user("Hello");
    context.addUserMessage(userMessage);

    List<ChatMessage> history = context.getHistory();
    
    assertThrows(UnsupportedOperationException.class, () -> {
      history.add(ChatMessage.assistant("Should not be able to add"));
    });
    
    assertThrows(UnsupportedOperationException.class, () -> {
      history.remove(0);
    });
    
    assertThrows(UnsupportedOperationException.class, () -> {
      history.clear();
    });
  }

  @Test
  void test_get_history_independent_copies() {
    ChatMessage userMessage = ChatMessage.user("Hello");
    context.addUserMessage(userMessage);

    List<ChatMessage> firstCopy = context.getHistory();
    List<ChatMessage> secondCopy = context.getHistory();

    assertEquals(firstCopy, secondCopy);
    assertTrue(firstCopy != secondCopy); // Different object references
  }

  @Test
  void test_to_string_empty_context() {
    String result = context.toString();
    assertEquals("", result);
  }

  @Test
  void test_to_string_single_user_message() {
    context.addUserMessage(ChatMessage.user("Hello"));

    String result = context.toString();
    assertEquals("User: Hello\n", result);
  }

  @Test
  void test_to_string_single_assistant_message() {
    context.addAssistantMessage(ChatMessage.assistant("Hi there!"));

    String result = context.toString();
    assertEquals("Assistant: Hi there!\n", result);
  }

  @Test
  void test_to_string_multiple_messages() {
    context.addUserMessage(ChatMessage.user("What is 2+2?"));
    context.addAssistantMessage(ChatMessage.assistant("2+2 equals 4"));
    context.addUserMessage(ChatMessage.user("Thank you!"));

    String result = context.toString();
    String expected = "User: What is 2+2?\n" +
                      "Assistant: 2+2 equals 4\n" +
                      "User: Thank you!\n";

    assertEquals(expected, result);
  }

  @Test
  void test_to_string_with_empty_messages() {
    context.addUserMessage(ChatMessage.user(""));
    context.addAssistantMessage(ChatMessage.assistant("Non-empty response"));
    context.addUserMessage(ChatMessage.user(""));

    String result = context.toString();
    String expected = "User: \n" +
                      "Assistant: Non-empty response\n" +
                      "User: \n";

    assertEquals(expected, result);
  }

  @Test
  void test_to_string_with_special_characters() {
    context.addUserMessage(ChatMessage.user("Message with\nnewlines"));
    context.addAssistantMessage(ChatMessage.assistant("Message with\ttabs and \"quotes\""));

    String result = context.toString();
    String expected = "User: Message with\nnewlines\n" +
                      "Assistant: Message with\ttabs and \"quotes\"\n";

    assertEquals(expected, result);
  }
}