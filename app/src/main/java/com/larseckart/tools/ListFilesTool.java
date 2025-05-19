package com.larseckart.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.larseckart.tool.ToolDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListFilesTool {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ToolDefinition create() {
        ObjectNode schema = objectMapper.createObjectNode();
        ObjectNode properties = schema.putObject("properties");
        ObjectNode pathProp = properties.putObject("path");
        pathProp.put("type", "string");
        pathProp.put("description", "Optional relative path to list files from. Defaults to current directory if not provided.");
        schema.put("type", "object");

        return new ToolDefinition(
                "list_files",
                "List files and directories at a given path. If no path is provided, lists files in the current directory.",
                schema,
                ListFilesTool::execute
        );
    }

    private static String execute(JsonNode input) {
        String dirPath = input.has("path") ? input.get("path").asText() : ".";
        try {
            Path path = Paths.get(dirPath);
            List<String> files = new ArrayList<>();
            
            try (Stream<Path> pathStream = Files.walk(path, 1)) {
                files = pathStream
                        .filter(p -> !p.equals(path))
                        .map(p -> {
                            String fileName = path.relativize(p).toString();
                            return Files.isDirectory(p) ? fileName + "/" : fileName;
                        })
                        .collect(Collectors.toList());
            }
            
            return objectMapper.writeValueAsString(files);
        } catch (IOException e) {
            return "Error listing files: " + e.getMessage();
        }
    }
}