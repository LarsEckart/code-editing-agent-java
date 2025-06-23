package com.larseckart.core.services;

import static org.assertj.core.api.Assertions.*;

import com.larseckart.ApiKey;
import com.larseckart.adapters.ai.AnthropicProvider;
import com.larseckart.core.domain.ConversationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ConversationServiceToolsTest {

  private ConversationContext context;
  private ApiKey apiKey;

  @BeforeEach
  void setUp() {
    context = new ConversationContext();
    apiKey = ApiKey.forTesting("test-api-key");
  }

  @Test
  void should_have_simplified_constructor() {
    // Test that ConversationService only has simple constructor
    ConversationService service = new ConversationService(context, new AnthropicProvider(apiKey));
    assertThat(service).isNotNull();

    // Check that only one constructor exists
    Constructor<?>[] constructors = ConversationService.class.getConstructors();
    assertThat(constructors).hasSize(1);

    // Check constructor parameters
    Class<?>[] paramTypes = constructors[0].getParameterTypes();
    assertThat(paramTypes).hasSize(2);
    assertThat(paramTypes[0]).isEqualTo(ConversationContext.class);
    assertThat(paramTypes[1].getName()).isEqualTo("com.larseckart.core.ports.AIProvider");
  }

  @Test
  void should_not_have_tool_registry_field() throws Exception {
    ConversationService service = new ConversationService(context, new AnthropicProvider(apiKey));

    // Check that toolRegistry field does not exist
    assertThatThrownBy(() -> service.getClass().getDeclaredField("toolRegistry"))
        .isInstanceOf(NoSuchFieldException.class);
  }

  @Test
  void should_not_have_tool_handling_methods() throws Exception {
    ConversationService service = new ConversationService(context, new AnthropicProvider(apiKey));

    // Check that tool handling methods do not exist
    assertThatThrownBy(
            () ->
                service
                    .getClass()
                    .getDeclaredMethod(
                        "handleToolUse", com.larseckart.core.domain.ai.AIResponse.class))
        .isInstanceOf(NoSuchMethodException.class);

    assertThatThrownBy(
            () ->
                service
                    .getClass()
                    .getDeclaredMethod("sendToolResultsToAIAndGetFinalResponse", String.class))
        .isInstanceOf(NoSuchMethodException.class);
  }

  @Test
  void should_have_sendMessage_method() throws Exception {
    ConversationService service = new ConversationService(context, new AnthropicProvider(apiKey));

    // Should have sendMessage method
    Method sendMessageMethod = service.getClass().getMethod("sendMessage", String.class);
    assertThat(sendMessageMethod).isNotNull();
    assertThat(sendMessageMethod.getName()).isEqualTo("sendMessage");
    assertThat(sendMessageMethod.getReturnType()).isEqualTo(String.class);
  }

  @Test
  void should_have_core_fields() throws Exception {
    ConversationService service = new ConversationService(context, new AnthropicProvider(apiKey));

    // Check if context field exists
    Field contextField = service.getClass().getDeclaredField("context");
    assertThat(contextField).isNotNull();
    assertThat(contextField.getType()).isEqualTo(ConversationContext.class);

    // Check if aiProvider field exists
    Field aiProviderField = service.getClass().getDeclaredField("aiProvider");
    assertThat(aiProviderField).isNotNull();
    assertThat(aiProviderField.getType().getName())
        .isEqualTo("com.larseckart.core.ports.AIProvider");
  }
}
