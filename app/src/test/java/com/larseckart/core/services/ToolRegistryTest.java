package com.larseckart.core.services;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.larseckart.core.domain.Tool;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ToolRegistryTest {

  private ToolRegistry toolRegistry;
  private ObjectMapper mapper;

  @BeforeEach
  void setUp() {
    toolRegistry = new ToolRegistry();
    mapper = new ObjectMapper();
  }

  @Test
  void should_register_and_retrieve_tool() {
    Tool mockTool =
        new Tool() {
          @Override
          public String getName() {
            return "testTool";
          }

          @Override
          public String getDescription() {
            return "A test tool";
          }

          @Override
          public String getParameterSchema() {
            return "{\"type\":\"object\",\"properties\":{\"param1\":{\"type\":\"string\"}}}";
          }

          @Override
          public String execute(JsonNode parameters) {
            return "executed";
          }

          @Override
          public void validate(JsonNode parameters) {
            // No validation for test
          }
        };

    toolRegistry.registerTool(mockTool);

    Tool retrieved = toolRegistry.getTool("testTool");
    assertThat(retrieved).isNotNull();
    assertThat(retrieved.getName()).isEqualTo("testTool");
    assertThat(retrieved.getDescription()).isEqualTo("A test tool");
  }

  @Test
  void should_return_null_for_unknown_tool() {
    Tool retrieved = toolRegistry.getTool("unknownTool");
    assertThat(retrieved).isNull();
  }

  @Test
  void should_get_all_registered_tools() {
    Tool tool1 = createMockTool("tool1", "First tool");
    Tool tool2 = createMockTool("tool2", "Second tool");

    toolRegistry.registerTool(tool1);
    toolRegistry.registerTool(tool2);

    Collection<Tool> allTools = toolRegistry.getAllTools();
    assertThat(allTools).hasSize(2);
    assertThat(allTools).contains(tool1);
    assertThat(allTools).contains(tool2);
  }

  @Test
  void should_convert_to_claude_function_definitions() {
    Tool mockTool =
        new Tool() {
          @Override
          public String getName() {
            return "searchTool";
          }

          @Override
          public String getDescription() {
            return "Search for information";
          }

          @Override
          public String getParameterSchema() {
            return "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\",\"description\":\"Search query\"},\"limit\":{\"type\":\"integer\",\"description\":\"Result limit\"}}}";
          }

          @Override
          public String execute(JsonNode parameters) {
            return "results";
          }

          @Override
          public void validate(JsonNode parameters) {
            // No validation for test
          }
        };

    toolRegistry.registerTool(mockTool);

    List<Map<String, Object>> functionDefs = toolRegistry.convertToClaudeFunctionDefinitions();
    assertThat(functionDefs).hasSize(1);

    Map<String, Object> functionDef = functionDefs.getFirst();
    assertThat(functionDef.get("name")).isEqualTo("searchTool");
    assertThat(functionDef.get("description")).isEqualTo("Search for information");

    @SuppressWarnings("unchecked")
    Map<String, Object> inputSchema = (Map<String, Object>) functionDef.get("input_schema");
    assertThat(inputSchema).isNotNull();
    assertThat(inputSchema.get("type")).isEqualTo("object");

    @SuppressWarnings("unchecked")
    Map<String, Object> properties = (Map<String, Object>) inputSchema.get("properties");
    assertThat(properties).isNotNull();
    assertThat(properties).containsKey("query");
    assertThat(properties).containsKey("limit");
  }

  @Test
  void should_route_function_call_to_correct_tool() {
    Tool mockTool =
        new Tool() {
          @Override
          public String getName() {
            return "calculator";
          }

          @Override
          public String getDescription() {
            return "Performs calculations";
          }

          @Override
          public String getParameterSchema() {
            return "{\"type\":\"object\",\"properties\":{\"expression\":{\"type\":\"string\"}}}";
          }

          @Override
          public String execute(JsonNode parameters) {
            return "Result: " + parameters.get("expression").asText();
          }

          @Override
          public void validate(JsonNode parameters) {
            // No validation for test
          }
        };

    toolRegistry.registerTool(mockTool);

    ObjectNode params = mapper.createObjectNode();
    params.put("expression", "2+2");

    String result = toolRegistry.routeFunctionCall("calculator", params);
    assertThat(result).isEqualTo("Result: 2+2");
  }

  @Test
  void should_throw_exception_when_routing_to_unknown_tool() {
    ObjectNode params = mapper.createObjectNode();

    assertThatThrownBy(() -> toolRegistry.routeFunctionCall("unknownTool", params))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_overwrite_tool_with_same_name() {
    Tool tool1 = createMockTool("sameName", "First version");
    Tool tool2 = createMockTool("sameName", "Second version");

    toolRegistry.registerTool(tool1);
    toolRegistry.registerTool(tool2);

    Collection<Tool> allTools = toolRegistry.getAllTools();
    assertThat(allTools).hasSize(1);

    Tool retrieved = toolRegistry.getTool("sameName");
    assertThat(retrieved.getDescription()).isEqualTo("Second version");
  }

  private Tool createMockTool(String name, String description) {
    return new Tool() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public String getDescription() {
        return description;
      }

      @Override
      public String getParameterSchema() {
        return "{\"type\":\"object\",\"properties\":{}}";
      }

      @Override
      public String execute(JsonNode parameters) {
        return "executed";
      }

      @Override
      public void validate(JsonNode parameters) {
        // No validation for test
      }
    };
  }
}
