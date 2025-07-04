package com.larseckart.adapters.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
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
  void show_welcome_message_should_print_welcome_message() {
    adapter.showWelcomeMessage();

    assertThat(outputStream.toString())
        .isEqualTo("Chat with the Agent (use 'ctrl-c' to quit or press Enter on empty line)\n");
  }

  @Test
  void prompt_for_user_input_should_print_prompt_with_blue_color() {
    adapter.promptForUserInput();

    assertThat(outputStream.toString()).isEqualTo("\u001b[94mYou\u001b[0m: ");
  }

  @Test
  void display_assistant_response_should_print_response_with_colors() {
    String testResponse = "Hello, how can I help you?";

    adapter.displayAssistantResponse(testResponse);

    assertThat(outputStream.toString())
        .isEqualTo("\u001b[95mTheAgent\u001b[0m: \u001b[92mHello, how can I help you?\u001b[0m\n");
  }

  @Test
  void display_assistant_response_should_handle_empty_response() {
    adapter.displayAssistantResponse("");

    assertThat(outputStream.toString())
        .isEqualTo("\u001b[95mTheAgent\u001b[0m: \u001b[92m\u001b[0m\n");
  }
}
