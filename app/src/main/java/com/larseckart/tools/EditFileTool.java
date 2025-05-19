package com.larseckart.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.larseckart.tool.ToolDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class EditFileTool {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ToolDefinition create() {
        ObjectNode schema = objectMapper.createObjectNode();
        ObjectNode properties = schema.putObject("properties");
        
        ObjectNode pathProp = properties.putObject("path");
        pathProp.put("type", "string");
        pathProp.put("description", "The path to the file");
        
        ObjectNode oldStrProp = properties.putObject("old_str");
        oldStrProp.put("type", "string");
        oldStrProp.put("description", "Text to search for - must match exactly and must only have one match exactly");
        
        ObjectNode newStrProp = properties.putObject("new_str");
        newStrProp.put("type", "string");
        newStrProp.put("description", "Text to replace old_str with");
        
        schema.putArray("required").add("path").add("old_str").add("new_str");
        schema.put("type", "object");

        return new ToolDefinition(
                "edit_file",
                """Make edits to a text file.
                
                Replaces 'old_str' with 'new_str' in the given file. 'old_str' and 'new_str' MUST be different from each other.
                
                If the file specified with path doesn't exist, it will be created.
                """,
                schema,
                EditFileTool::execute
        );
    }

    private static String execute(JsonNode input) {
        String filePath = input.get("path").asText();
        String oldStr = input.get("old_str").asText();
        String newStr = input.get("new_str").asText();
        
        if (oldStr.equals(newStr)) {
            return "Error: old_str and new_str must be different";
        }
        
        try {
            Path path = Paths.get(filePath);
            // Handle file creation if it doesn't exist
            if (!Files.exists(path) && oldStr.isEmpty()) {
                return createNewFile(path, newStr);
            }
            
            // Read and modify existing file
            String content = Files.readString(path);
            String newContent = content.replace(oldStr, newStr);
            
            if (content.equals(newContent) && !oldStr.isEmpty()) {
                return "Error: old_str not found in file";
            }
            
            Files.writeString(path, newContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "OK";
            
        } catch (IOException e) {
            return "Error editing file: " + e.getMessage();
        }
    }
    
    private static String createNewFile(Path filePath, String content) throws IOException {
        // Create parent directories if they don't exist
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return "Successfully created file " + filePath;
    }
}