package com.larseckart.core.services;

import com.anthropic.models.messages.ToolUnion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.larseckart.core.domain.Tool;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ToolRegistry {
  
  private final Map<String, Tool> tools = new ConcurrentHashMap<>();
  
  public void registerTool(Tool tool) {
    tools.put(tool.getName(), tool);
  }
  
  public Tool getTool(String name) {
    return tools.get(name);
  }
  
  public Collection<Tool> getAllTools() {
    return new ArrayList<>(tools.values());
  }
  
  public List<Map<String, Object>> convertToClaudeFunctionDefinitions() {
    List<Map<String, Object>> functionDefinitions = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    
    for (Tool tool : tools.values()) {
      Map<String, Object> functionDef = new HashMap<>();
      functionDef.put("name", tool.getName());
      functionDef.put("description", tool.getDescription());
      
      try {
        // Parse the JSON schema string
        JsonNode schemaNode = mapper.readTree(tool.getParameterSchema());
        @SuppressWarnings("unchecked")
        Map<String, Object> inputSchema = mapper.convertValue(schemaNode, Map.class);
        
        functionDef.put("input_schema", inputSchema);
        functionDefinitions.add(functionDef);
      } catch (Exception e) {
        throw new RuntimeException("Failed to parse parameter schema for tool: " + tool.getName(), e);
      }
    }
    
    return functionDefinitions;
  }
  
  public String routeFunctionCall(String toolName, JsonNode parameters) {
    Tool tool = getTool(toolName);
    if (tool == null) {
      throw new IllegalArgumentException("Unknown tool: " + toolName);
    }
    return tool.execute(parameters);
  }
}