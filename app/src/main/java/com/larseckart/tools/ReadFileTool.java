package com.larseckart.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.larseckart.tool.ToolDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadFileTool {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ToolDefinition create() {
        ObjectNode schema = objectMapper.createObjectNode();
        ObjectNode properties = schema.putObject("properties");
        ObjectNode pathProp = properties.putObject("path");
        pathProp.put("type", "string");
        pathProp.put("description", "The relative path of a file in the working directory.");
        schema.putArray("required").add("path");
        schema.put("type", "object");

        return new ToolDefinition(
                "read_file",
                "Read the contents of a given relative file path. Use this when you want to see what's inside a file. Do not use this with directory names.",
                schema,
                ReadFileTool::execute
        );
    }

    private static String execute(JsonNode input) {
        String filePath = input.get("path").asText();
        try {
            Path path = Paths.get(filePath);
            return Files.readString(path);
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }
}