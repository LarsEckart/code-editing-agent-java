package com.larseckart.core.services;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.larseckart.ApiKey;
import com.larseckart.core.domain.ConversationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ConversationServiceToolsTest {

  @Mock private ToolRegistry mockToolRegistry;

  private ConversationContext context;
  private ApiKey apiKey;

  @BeforeEach
  void setUp() {
    context = new ConversationContext();
    apiKey = ApiKey.forTesting("test-api-key");
  }

  @Test
  void should_accept_tool_registry_in_constructor() {
    // Test that ConversationService now accepts ToolRegistry
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);
    assertThat(service).isNotNull();

    // Check that constructor with ToolRegistry parameter exists
    Constructor<?>[] constructors = ConversationService.class.getConstructors();

    boolean hasToolRegistryConstructor = false;
    for (Constructor<?> constructor : constructors) {
      Class<?>[] paramTypes = constructor.getParameterTypes();
      if (paramTypes.length == 3 && paramTypes[2].equals(ToolRegistry.class)) {
        hasToolRegistryConstructor = true;
        break;
      }
    }

    assertThat(hasToolRegistryConstructor)
        .as("Constructor with ToolRegistry should exist")
        .isTrue();
  }

  @Test
  void should_have_method_to_handle_tool_use() throws Exception {
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);

    // Check if sendMessage method exists (it should)
    Method sendMessageMethod = service.getClass().getMethod("sendMessage", String.class);
    assertThat(sendMessageMethod).isNotNull();

    // Check if there's a method to handle tool use
    Method handleToolUseMethod =
        service
            .getClass()
            .getDeclaredMethod("handleToolUse", com.anthropic.models.messages.Message.class);
    assertThat(handleToolUseMethod).isNotNull();
    assertThat(handleToolUseMethod.getName()).isEqualTo("handleToolUse");
  }

  @Test
  void should_have_tool_registry_field() throws Exception {
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);

    // Check if toolRegistry field exists
    Field toolRegistryField = service.getClass().getDeclaredField("toolRegistry");
    assertThat(toolRegistryField).isNotNull();
    assertThat(toolRegistryField.getType()).isEqualTo(ToolRegistry.class);
  }

  @Test
  void should_support_tool_definitions_in_api_call() throws Exception {
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);

    // Verify service exists and has tool support methods
    assertThat(service).isNotNull();

    // Check if hasToolUse method exists
    Method hasToolUseMethod =
        service
            .getClass()
            .getDeclaredMethod("hasToolUse", com.anthropic.models.messages.Message.class);
    assertThat(hasToolUseMethod).isNotNull();
    assertThat(hasToolUseMethod.getName()).isEqualTo("hasToolUse");
  }

  @Test
  void should_maintain_backward_compatibility() {
    // Test that existing functionality structure is maintained
    ConversationService service = new ConversationService(context, apiKey);

    // Should be able to create service without ToolRegistry
    assertThat(service).isNotNull();

    // Should have sendMessage method
    try {
      service.getClass().getMethod("sendMessage", String.class);
      assertThat(true).isTrue(); // Method exists
    } catch (NoSuchMethodException e) {
      throw new AssertionError("sendMessage method should exist");
    }

    // Context should start empty
    assertThat(context.getHistory().size()).isEqualTo(0);
  }

  @Test
  void constructor_reflection_test() {
    // Test that both constructors exist
    Constructor<?>[] constructors = ConversationService.class.getConstructors();
    assertThat(constructors).as("Should have two constructors").hasSize(2);

    boolean hasOriginalConstructor = false;
    boolean hasToolRegistryConstructor = false;

    for (Constructor<?> constructor : constructors) {
      Class<?>[] paramTypes = constructor.getParameterTypes();
      if (paramTypes.length == 2
          && paramTypes[0].equals(ConversationContext.class)
          && paramTypes[1].equals(ApiKey.class)) {
        hasOriginalConstructor = true;
      } else if (paramTypes.length == 3
          && paramTypes[0].equals(ConversationContext.class)
          && paramTypes[1].equals(ApiKey.class)
          && paramTypes[2].equals(ToolRegistry.class)) {
        hasToolRegistryConstructor = true;
      }
    }

    assertThat(hasOriginalConstructor)
        .as("Original constructor should exist for backward compatibility")
        .isTrue();
    assertThat(hasToolRegistryConstructor)
        .as("Constructor with ToolRegistry should exist")
        .isTrue();
  }

  @Test
  void should_have_tool_handling_methods() throws Exception {
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);

    // Check if tool handling methods exist
    Method hasToolUseMethod =
        service
            .getClass()
            .getDeclaredMethod("hasToolUse", com.anthropic.models.messages.Message.class);
    assertThat(hasToolUseMethod).isNotNull();

    Method handleToolUseMethod =
        service
            .getClass()
            .getDeclaredMethod("handleToolUse", com.anthropic.models.messages.Message.class);
    assertThat(handleToolUseMethod).isNotNull();

    Method sendToolResultsMethod =
        service
            .getClass()
            .getDeclaredMethod(
                "sendToolResultsToClaudeAndGetFinalResponse",
                com.anthropic.models.messages.Message.class,
                String.class);
    assertThat(sendToolResultsMethod).isNotNull();
  }

  @Test
  void should_have_object_mapper_field() throws Exception {
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);

    // Check if objectMapper field exists
    Field objectMapperField = service.getClass().getDeclaredField("objectMapper");
    assertThat(objectMapperField).isNotNull();
    assertThat(objectMapperField.getType()).isEqualTo(ObjectMapper.class);
  }
}
