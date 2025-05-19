package com.larseckart.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.function.Function;

public class ToolDefinition {
    private final String name;
    private final String description;
    private final ObjectNode inputSchema;
    private final Function<JsonNode, String> function;

    public ToolDefinition(String name, String description, ObjectNode inputSchema, Function<JsonNode, String> function) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ObjectNode getInputSchema() {
        return inputSchema;
    }

    public String execute(JsonNode input) {
        try {
            return function.apply(input);
        } catch (Exception e) {
            return "Error executing tool: " + e.getMessage();
        }
    }
}