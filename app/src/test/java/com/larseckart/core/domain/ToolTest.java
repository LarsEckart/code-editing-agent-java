package com.larseckart.core.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ToolTest {

  private ObjectMapper objectMapper;
  private Tool mockTool;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    mockTool = new MockTool();
  }

  @Test
  void should_have_get_name_method() {
    String name = mockTool.getName();
    assertNotNull(name);
    assertFalse(name.trim().isEmpty());
  }

  @Test
  void should_have_get_description_method() {
    String description = mockTool.getDescription();
    assertNotNull(description);
    assertFalse(description.trim().isEmpty());
  }

  @Test
  void should_have_get_parameter_schema_method() {
    String schema = mockTool.getParameterSchema();
    assertNotNull(schema);
    assertFalse(schema.trim().isEmpty());
    // Verify it's valid JSON
    assertDoesNotThrow(() -> objectMapper.readTree(schema));
  }

  @Test
  void should_have_execute_method() throws Exception {
    JsonNode parameters = objectMapper.createObjectNode();
    String result = mockTool.execute(parameters);
    assertNotNull(result);
  }

  @Test
  void should_handle_null_parameters_in_execute() {
    assertThrows(IllegalArgumentException.class, () -> mockTool.execute(null));
  }

  @Test
  void should_have_validate_method() throws Exception {
    JsonNode validParameters = objectMapper.createObjectNode();
    assertDoesNotThrow(() -> mockTool.validate(validParameters));
  }

  @Test
  void should_throw_on_invalid_parameters_in_validate() {
    assertThrows(IllegalArgumentException.class, () -> mockTool.validate(null));
  }

  @Test
  void should_return_consistent_name() {
    String name1 = mockTool.getName();
    String name2 = mockTool.getName();
    assertEquals(name1, name2);
  }

  @Test
  void should_return_consistent_description() {
    String desc1 = mockTool.getDescription();
    String desc2 = mockTool.getDescription();
    assertEquals(desc1, desc2);
  }

  @Test
  void should_return_consistent_parameter_schema() {
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