package com.larseckart.core.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.larseckart.core.domain.Tool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EditFileTool performs simple text replacement operations on files. It creates a backup of the
 * original file before making any changes and validates that the search text exists before
 * replacement.
 */
public class EditFileTool implements Tool {

  private static final Logger logger = LoggerFactory.getLogger(EditFileTool.class);

  @Override
  public String getName() {
    return "edit_file";
  }

  @Override
  public String getDescription() {
    return "Performs simple text replacement in files. Creates a backup before editing and validates that search text exists.";
  }

  @Override
  public String getParameterSchema() {
    return """
        {
          "type": "object",
          "properties": {
            "path": {
              "type": "string",
              "description": "The path to the file to edit"
            },
            "search_text": {
              "type": "string",
              "description": "The text to search for and replace"
            },
            "replace_text": {
              "type": "string",
              "description": "The text to replace the search text with"
            }
          },
          "required": ["path", "search_text", "replace_text"]
        }""";
  }

  @Override
  public void validate(JsonNode parameters) {
    if (parameters == null) {
      throw new IllegalArgumentException("Parameters cannot be null");
    }

    if (!parameters.has("path") || parameters.get("path").asText().trim().isEmpty()) {
      throw new IllegalArgumentException("'path' parameter is required and cannot be empty");
    }

    if (!parameters.has("search_text") || parameters.get("search_text").asText().trim().isEmpty()) {
      throw new IllegalArgumentException("'search_text' parameter is required and cannot be empty");
    }

    if (!parameters.has("replace_text")) {
      throw new IllegalArgumentException("'replace_text' parameter is required");
    }
  }

  @Override
  public String execute(JsonNode parameters) {
    logger.info("Executing edit_file tool with parameters: {}", parameters);

    try {
      // Validate required parameters
      if (!parameters.has("path")) {
        return "Error: 'path' parameter is required";
      }
      if (!parameters.has("search_text")) {
        return "Error: 'search_text' parameter is required";
      }
      if (!parameters.has("replace_text")) {
        return "Error: 'replace_text' parameter is required";
      }

      String pathStr = parameters.get("path").asText();
      String searchText = parameters.get("search_text").asText();
      String replaceText = parameters.get("replace_text").asText();

      // Validate path for security (prevent directory traversal)
      if (pathStr.contains("..")
          || pathStr.startsWith("/etc/")
          || pathStr.startsWith("/usr/")
          || pathStr.startsWith("/bin/")) {
        logger.warn("Potentially unsafe path attempted: {}", pathStr);
        return "Error: Path not allowed for security reasons";
      }

      Path filePath;
      try {
        filePath = Paths.get(pathStr);
        if (!filePath.isAbsolute()) {
          filePath = Paths.get(System.getProperty("user.dir")).resolve(pathStr);
        }
        filePath = filePath.normalize();
      } catch (Exception e) {
        logger.error("Invalid path: {}", pathStr, e);
        return "Error: Invalid file path: " + pathStr;
      }

      // Check if file exists
      if (!Files.exists(filePath)) {
        logger.error("File not found: {}", filePath);
        return "Error: File not found: " + pathStr;
      }

      // Check if it's actually a file (not a directory)
      if (!Files.isRegularFile(filePath)) {
        logger.error("Path is not a regular file: {}", filePath);
        return "Error: Path is not a regular file: " + pathStr;
      }

      // Read current file content
      String content;
      try {
        content = Files.readString(filePath);
      } catch (IOException e) {
        logger.error("Failed to read file: {}", filePath, e);
        return "Error: Failed to read file: " + e.getMessage();
      }

      // Check if search text exists in the file
      if (!content.contains(searchText)) {
        logger.info("Search text '{}' not found in file: {}", searchText, filePath);
        return "Error: Text '" + searchText + "' not found in file";
      }

      // Count occurrences before replacement
      int occurrences = content.split(searchText, -1).length - 1;

      // Create backup file
      Path backupPath = Paths.get(filePath + ".backup");
      try {
        Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Created backup file: {}", backupPath);
      } catch (IOException e) {
        logger.error("Failed to create backup file: {}", backupPath, e);
        return "Error: Failed to create backup file: " + e.getMessage();
      }

      // Perform replacement
      String newContent = content.replace(searchText, replaceText);

      // Write updated content back to file
      try {
        Files.writeString(filePath, newContent);
        logger.info(
            "Successfully edited file: {} ({} occurrences replaced)", filePath, occurrences);
      } catch (IOException e) {
        logger.error("Failed to write updated content to file: {}", filePath, e);
        return "Error: Failed to write to file: " + e.getMessage();
      }

      return String.format(
          "File edited successfully! Replaced %d occurrences of '%s' with '%s' in %s. Backup created at %s.backup",
          occurrences, searchText, replaceText, pathStr, pathStr);

    } catch (Exception e) {
      logger.error("Unexpected error in edit_file tool", e);
      return "Error: Unexpected error occurred: " + e.getMessage();
    }
  }
}
