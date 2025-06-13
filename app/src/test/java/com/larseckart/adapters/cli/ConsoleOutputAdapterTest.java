package com.larseckart.adapters.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleOutputAdapterTest {

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private ConsoleOutputAdapter adapter;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outputStream));
    adapter = new ConsoleOutputAdapter();
  }

  @AfterEach
  void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  void showWelcomeMessage_shouldPrintWelcomeMessage() {
    adapter.showWelcomeMessage();

    assertThat(outputStream.toString())
        .isEqualTo("Chat with Claude (use 'ctrl-c' to quit or press Enter on empty line)\n");
  }

  @Test
  void promptForUserInput_shouldPrintPromptWithBlueColor() {
    adapter.promptForUserInput();

    assertThat(outputStream.toString())
        .isEqualTo("\u001b[94mYou\u001b[0m: ");
  }

  @Test
  void displayAssistantResponse_shouldPrintResponseWithColors() {
    String testResponse = "Hello, how can I help you?";

    adapter.displayAssistantResponse(testResponse);

    assertThat(outputStream.toString())
        .isEqualTo("\u001b[95mClaude\u001b[0m: \u001b[92mHello, how can I help you?\u001b[0m\n");
  }

  @Test
  void displayAssistantResponse_shouldHandleEmptyResponse() {
    adapter.displayAssistantResponse("");

    assertThat(outputStream.toString())
        .isEqualTo("\u001b[95mClaude\u001b[0m: \u001b[92m\u001b[0m\n");
  }

}