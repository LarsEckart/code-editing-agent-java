package com.larseckart.core.domain;

import static org.assertj.core.api.Assertions.*;

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
    assertThat(history.isEmpty()).isTrue();
  }

  @Test
  void test_add_user_message() {
    ChatMessage userMessage = ChatMessage.user("Hello");
    context.addUserMessage(userMessage);

    List<ChatMessage> history = context.getHistory();
    assertThat(history.size()).isEqualTo(1);
    assertThat(history.getFirst()).isEqualTo(userMessage);
  }

  @Test
  void test_add_assistant_message() {
    ChatMessage assistantMessage = ChatMessage.assistant("Hi there!");
    context.addAssistantMessage(assistantMessage);

    List<ChatMessage> history = context.getHistory();
    assertThat(history.size()).isEqualTo(1);
    assertThat(history.getFirst()).isEqualTo(assistantMessage);
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
    assertThat(history.size()).isEqualTo(4);
    assertThat(history.get(0)).isEqualTo(userMessage1);
    assertThat(history.get(1)).isEqualTo(assistantMessage1);
    assertThat(history.get(2)).isEqualTo(userMessage2);
    assertThat(history.get(3)).isEqualTo(assistantMessage2);
  }

  @Test
  void test_get_history_returns_immutable_list() {
    ChatMessage userMessage = ChatMessage.user("Hello");
    context.addUserMessage(userMessage);

    List<ChatMessage> history = context.getHistory();

    assertThatThrownBy(() -> history.add(ChatMessage.assistant("Should not be able to add")))
        .isInstanceOf(UnsupportedOperationException.class);

    assertThatThrownBy(history::removeFirst).isInstanceOf(UnsupportedOperationException.class);

    assertThatThrownBy(history::clear).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void test_get_history_independent_copies() {
    ChatMessage userMessage = ChatMessage.user("Hello");
    context.addUserMessage(userMessage);

    List<ChatMessage> firstCopy = context.getHistory();
    List<ChatMessage> secondCopy = context.getHistory();

    assertThat(firstCopy).isEqualTo(secondCopy);
    assertThat(firstCopy != secondCopy).isTrue(); // Different object references
  }

  @Test
  void test_to_string_empty_context() {
    String result = context.toString();
    assertThat(result).isEqualTo("");
  }

  @Test
  void test_to_string_single_user_message() {
    context.addUserMessage(ChatMessage.user("Hello"));

    String result = context.toString();
    assertThat(result).isEqualTo("User: Hello\n");
  }

  @Test
  void test_to_string_single_assistant_message() {
    context.addAssistantMessage(ChatMessage.assistant("Hi there!"));

    String result = context.toString();
    assertThat(result).isEqualTo("Assistant: Hi there!\n");
  }

  @Test
  void test_to_string_multiple_messages() {
    context.addUserMessage(ChatMessage.user("What is 2+2?"));
    context.addAssistantMessage(ChatMessage.assistant("2+2 equals 4"));
    context.addUserMessage(ChatMessage.user("Thank you!"));

    String result = context.toString();
    String expected =
        """
        User: What is 2+2?
        Assistant: 2+2 equals 4
        User: Thank you!
        """;

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void test_to_string_with_empty_messages() {
    context.addUserMessage(ChatMessage.user(""));
    context.addAssistantMessage(ChatMessage.assistant("Non-empty response"));
    context.addUserMessage(ChatMessage.user(""));

    String result = context.toString();
    String expected =
        """
        User:\s
        Assistant: Non-empty response
        User:\s
        """;

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void test_to_string_with_special_characters() {
    context.addUserMessage(ChatMessage.user("Message with\nnewlines"));
    context.addAssistantMessage(ChatMessage.assistant("Message with\ttabs and \"quotes\""));

    String result = context.toString();
    String expected =
        """
            User: Message with
            newlines
            Assistant: Message with\ttabs and "quotes"
            """;

    assertThat(result).isEqualTo(expected);
  }
}
