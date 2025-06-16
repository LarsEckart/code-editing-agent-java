package com.larseckart.core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.larseckart.ApiKey;
import com.larseckart.core.domain.ConversationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceToolsTest {

  @Mock
  private ToolRegistry mockToolRegistry;

  private ConversationContext context;
  private ApiKey apiKey;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    context = new ConversationContext();
    apiKey = ApiKey.forTesting("test-api-key");
    objectMapper = new ObjectMapper();
  }

  @Test
  void shouldAcceptToolRegistryInConstructor() {
    // Test that ConversationService now accepts ToolRegistry
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);
    assertNotNull(service);
    
    // Check that constructor with ToolRegistry parameter exists
    Constructor<?>[] constructors = ConversationService.class.getConstructors();
    
    boolean hasToolRegistryConstructor = false;
    for (Constructor<?> constructor : constructors) {
      Class<?>[] paramTypes = constructor.getParameterTypes();
      if (paramTypes.length == 3 && 
          paramTypes[2].equals(ToolRegistry.class)) {
        hasToolRegistryConstructor = true;
        break;
      }
    }
    
    assertTrue(hasToolRegistryConstructor, "Constructor with ToolRegistry should exist");
  }

  @Test
  void shouldHaveMethodToHandleToolUse() throws Exception {
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);
    
    // Check if sendMessage method exists (it should)
    Method sendMessageMethod = service.getClass().getMethod("sendMessage", String.class);
    assertNotNull(sendMessageMethod);
    
    // Check if there's a method to handle tool use
    Method handleToolUseMethod = service.getClass().getDeclaredMethod("handleToolUse", 
        com.anthropic.models.messages.Message.class);
    assertNotNull(handleToolUseMethod);
    assertEquals("handleToolUse", handleToolUseMethod.getName());
  }

  @Test
  void shouldHaveToolRegistryField() throws Exception {
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);
    
    // Check if toolRegistry field exists
    Field toolRegistryField = service.getClass().getDeclaredField("toolRegistry");
    assertNotNull(toolRegistryField);
    assertEquals(ToolRegistry.class, toolRegistryField.getType());
  }

  @Test
  void shouldSupportToolDefinitionsInApiCall() throws Exception {
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);
    
    // Verify service exists and has tool support methods
    assertNotNull(service);
    
    // Check if hasToolUse method exists
    Method hasToolUseMethod = service.getClass().getDeclaredMethod("hasToolUse", 
        com.anthropic.models.messages.Message.class);
    assertNotNull(hasToolUseMethod);
    assertEquals("hasToolUse", hasToolUseMethod.getName());
  }

  @Test
  void shouldMaintainBackwardCompatibility() {
    // Test that existing functionality structure is maintained
    ConversationService service = new ConversationService(context, apiKey);
    
    // Should be able to create service without ToolRegistry
    assertNotNull(service);
    
    // Should have sendMessage method
    try {
      service.getClass().getMethod("sendMessage", String.class);
      assertTrue(true); // Method exists
    } catch (NoSuchMethodException e) {
      fail("sendMessage method should exist");
    }
    
    // Context should start empty
    assertEquals(0, context.getHistory().size());
  }

  @Test
  void constructorReflectionTest() {
    // Test that both constructors exist
    Constructor<?>[] constructors = ConversationService.class.getConstructors();
    assertEquals(2, constructors.length, "Should have two constructors");
    
    boolean hasOriginalConstructor = false;
    boolean hasToolRegistryConstructor = false;
    
    for (Constructor<?> constructor : constructors) {
      Class<?>[] paramTypes = constructor.getParameterTypes();
      if (paramTypes.length == 2 && 
          paramTypes[0].equals(ConversationContext.class) &&
          paramTypes[1].equals(ApiKey.class)) {
        hasOriginalConstructor = true;
      } else if (paramTypes.length == 3 && 
          paramTypes[0].equals(ConversationContext.class) &&
          paramTypes[1].equals(ApiKey.class) &&
          paramTypes[2].equals(ToolRegistry.class)) {
        hasToolRegistryConstructor = true;
      }
    }
    
    assertTrue(hasOriginalConstructor, "Original constructor should exist for backward compatibility");
    assertTrue(hasToolRegistryConstructor, "Constructor with ToolRegistry should exist");
  }

  @Test
  void shouldHaveToolHandlingMethods() throws Exception {
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);
    
    // Check if tool handling methods exist
    Method hasToolUseMethod = service.getClass().getDeclaredMethod("hasToolUse", 
        com.anthropic.models.messages.Message.class);
    assertNotNull(hasToolUseMethod);
    
    Method handleToolUseMethod = service.getClass().getDeclaredMethod("handleToolUse", 
        com.anthropic.models.messages.Message.class);
    assertNotNull(handleToolUseMethod);
    
    Method sendToolResultsMethod = service.getClass().getDeclaredMethod("sendToolResultsToClaudeAndGetFinalResponse", 
        com.anthropic.models.messages.Message.class, String.class);
    assertNotNull(sendToolResultsMethod);
  }

  @Test
  void shouldHaveObjectMapperField() throws Exception {
    ConversationService service = new ConversationService(context, apiKey, mockToolRegistry);
    
    // Check if objectMapper field exists
    Field objectMapperField = service.getClass().getDeclaredField("objectMapper");
    assertNotNull(objectMapperField);
    assertEquals(ObjectMapper.class, objectMapperField.getType());
  }
}