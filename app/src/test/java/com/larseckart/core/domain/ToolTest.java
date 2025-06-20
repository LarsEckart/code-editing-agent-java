package com.larseckart.core.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

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
    assertThat(name).isNotNull();
    assertThat(name.trim().isEmpty()).isFalse();
  }

  @Test
  void should_have_get_description_method() {
    String description = mockTool.getDescription();
    assertThat(description).isNotNull();
    assertThat(description.trim().isEmpty()).isFalse();
  }

  @Test
  void should_have_get_parameter_schema_method() {
    String schema = mockTool.getParameterSchema();
    assertThat(schema).isNotNull();
    assertThat(schema.trim().isEmpty()).isFalse();
    // Verify it's valid JSON
    assertThatCode(() -> objectMapper.readTree(schema)).doesNotThrowAnyException();
  }

  @Test
  void should_have_execute_method() throws Exception {
    JsonNode parameters = objectMapper.createObjectNode();
    String result = mockTool.execute(parameters);
    assertThat(result).isNotNull();
  }

  @Test
  void should_handle_null_parameters_in_execute() {
    assertThatThrownBy(() -> mockTool.execute(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_have_validate_method() throws Exception {
    JsonNode validParameters = objectMapper.createObjectNode();
    assertThatCode(() -> mockTool.validate(validParameters)).doesNotThrowAnyException();
  }

  @Test
  void should_throw_on_invalid_parameters_in_validate() {
    assertThatThrownBy(() -> mockTool.validate(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_return_consistent_name() {
    String name1 = mockTool.getName();
    String name2 = mockTool.getName();
    assertThat(name1).isEqualTo(name2);
  }

  @Test
  void should_return_consistent_description() {
    String desc1 = mockTool.getDescription();
    String desc2 = mockTool.getDescription();
    assertThat(desc1).isEqualTo(desc2);
  }

  @Test
  void should_return_consistent_parameter_schema() {
    String schema1 = mockTool.getParameterSchema();
    String schema2 = mockTool.getParameterSchema();
    assertThat(schema1).isEqualTo(schema2);
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