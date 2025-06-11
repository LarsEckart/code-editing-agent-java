package com.larseckart;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContextTest {

  private Context context;

  @BeforeEach
  void setUp() {
    context = new Context();
  }

  @Test
  void testToString_emptyContext() {
    String result = context.toString();
    assertEquals("", result);
  }

  @Test
  void testToString_singleUserMessage() {
    context.append("Hello");

    String result = context.toString();
    assertEquals("user: Hello\n", result);
  }

  @Test
  void testToString_userAndSystemMessages() {
    context.append("What is 2+2?");
    context.append("2+2 equals 4");

    String result = context.toString();
    assertEquals("user: What is 2+2?\nsystem: 2+2 equals 4\n", result);
  }

  @Test
  void testToString_alternatingRoles() {
    context.append("First user message");
    context.append("First system response");
    context.append("Second user message");
    context.append("Second system response");
    context.append("Third user message");

    String result = context.toString();
    String expected = "user: First user message\n" +
                      "system: First system response\n" +
                      "user: Second user message\n" +
                      "system: Second system response\n" +
                      "user: Third user message\n";

    assertEquals(expected, result);
  }

  @Test
  void testToString_withEmptyMessages() {
    context.append("");
    context.append("Non-empty response");
    context.append("");

    String result = context.toString();
    String expected = "user: \n" +
                      "system: Non-empty response\n" +
                      "user: \n";

    assertEquals(expected, result);
  }

  @Test
  void testToString_withSpecialCharacters() {
    context.append("Message with\nnewlines");
    context.append("Message with\ttabs and \"quotes\"");

    String result = context.toString();
    String expected = "user: Message with\nnewlines\n" +
                      "system: Message with\ttabs and \"quotes\"\n";

    assertEquals(expected, result);
  }
}
