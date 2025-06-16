package com.larseckart.core.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolTest {

  private ObjectMapper objectMapper;
  private Tool mockTool;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    mockTool = new MockTool();
  }

  @Test
  void shouldHaveGetNameMethod() {
    String name = mockTool.getName();
    assertNotNull(name);
    assertFalse(name.trim().isEmpty());
  }

  @Test
  void shouldHaveGetDescriptionMethod() {
    String description = mockTool.getDescription();
    assertNotNull(description);
    assertFalse(description.trim().isEmpty());
  }

  @Test
  void shouldHaveGetParameterSchemaMethod() {
    String schema = mockTool.getParameterSchema();
    assertNotNull(schema);
    assertFalse(schema.trim().isEmpty());
    // Verify it's valid JSON
    assertDoesNotThrow(() -> objectMapper.readTree(schema));
  }

  @Test
  void shouldHaveExecuteMethod() throws Exception {
    JsonNode parameters = objectMapper.createObjectNode();
    String result = mockTool.execute(parameters);
    assertNotNull(result);
  }

  @Test
  void shouldHandleNullParametersInExecute() {
    assertThrows(IllegalArgumentException.class, () -> mockTool.execute(null));
  }

  @Test
  void shouldHaveValidateMethod() throws Exception {
    JsonNode validParameters = objectMapper.createObjectNode();
    assertDoesNotThrow(() -> mockTool.validate(validParameters));
  }

  @Test
  void shouldThrowOnInvalidParametersInValidate() {
    assertThrows(IllegalArgumentException.class, () -> mockTool.validate(null));
  }

  @Test
  void shouldReturnConsistentName() {
    String name1 = mockTool.getName();
    String name2 = mockTool.getName();
    assertEquals(name1, name2);
  }

  @Test
  void shouldReturnConsistentDescription() {
    String desc1 = mockTool.getDescription();
    String desc2 = mockTool.getDescription();
    assertEquals(desc1, desc2);
  }

  @Test
  void shouldReturnConsistentParameterSchema() {
    String schema1 = mockTool.getParameterSchema();
    String schema2 = mockTool.getParameterSchema();
    assertEquals(schema1, schema2);
  }

  // Mock implementation for testing interface contract
  private static class MockTool implements Tool {
    @Override
    public String getName() {
      return "mock-tool";
    }

    @Override
    public String getDescription() {
      return "A mock tool for testing";
    }

    @Override
    public String getParameterSchema() {
      return "{\"type\":\"object\",\"properties\":{}}";
    }

    @Override
    public String execute(JsonNode parameters) {
      if (parameters == null) {
        throw new IllegalArgumentException("Parameters cannot be null");
      }
      return "mock result";
    }

    @Override
    public void validate(JsonNode parameters) {
      if (parameters == null) {
        throw new IllegalArgumentException("Parameters cannot be null");
      }
    }
  }
}