package com.larseckart.core.tools;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.JsonNode;
import com.larseckart.core.domain.Tool;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;

/**
 * A tool that reads file contents from the filesystem.
 * Supports both absolute and relative paths, various encodings, and includes
 * proper error handling and file size limits.
 */
public class ReadFileTool implements Tool {

  private static final Logger log = getLogger(ReadFileTool.class);

  private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB limit
  private static final String DEFAULT_ENCODING = "UTF-8";

  @Override
  public String getName() {
    return "read_file";
  }

  @Override
  public String getDescription() {
    return "Reads file contents from the filesystem. Supports both absolute and relative paths, " +
           "various text encodings, and includes proper error handling for common filesystem issues.";
  }

  @Override
  public String getParameterSchema() {
    return """
      {
        "$schema": "https://json-schema.org/draft/2020-12/schema",
        "type": "object",
        "properties": {
          "path": {
            "type": "string",
            "description": "The file path to read (absolute or relative to current working directory)"
          },
          "encoding": {
            "type": "string",
            "description": "The character encoding to use (default: UTF-8)"
          }
        },
        "required": ["path"],
        "additionalProperties": false
      }""";
  }

  @Override
  public String execute(JsonNode parameters) {
    log.info("Executing ReadFileTool with parameters: {}", parameters);
    if (parameters == null) {
      throw new IllegalArgumentException("Parameters cannot be null");
    }

    try {
      validate(parameters);
      
      String pathStr = parameters.get("path").asText();
      String encoding = parameters.has("encoding") ? 
        parameters.get("encoding").asText() : DEFAULT_ENCODING;

      Path path = resolveFilePath(pathStr);
      
      // Check file size before reading
      if (Files.exists(path)) {
        long fileSize = Files.size(path);
        if (fileSize > MAX_FILE_SIZE) {
          return "Error: File is too large (" + fileSize + " bytes). Maximum supported file size is " + 
                 MAX_FILE_SIZE + " bytes.";
        }
      }

      // Read file content with specified encoding
      Charset charset = Charset.forName(encoding);
      String content = Files.readString(path, charset);
      return content;

    } catch (NoSuchFileException e) {
      return "Error: File not found: " + e.getFile();
    } catch (SecurityException e) {
      return "Error: Permission denied accessing file: " + parameters.get("path").asText();
    } catch (IOException e) {
      return "Error: IO exception reading file: " + e.getMessage();
    } catch (IllegalArgumentException e) {
      return "Error: Invalid encoding specified: " + parameters.get("encoding").asText();
    } catch (Exception e) {
      return "Error: Unexpected error reading file: " + e.getMessage();
    }
  }

  @Override
  public void validate(JsonNode parameters) {
    if (parameters == null) {
      throw new IllegalArgumentException("Parameters cannot be null");
    }

    if (!parameters.has("path")) {
      throw new IllegalArgumentException("Required parameter 'path' is missing");
    }

    JsonNode pathNode = parameters.get("path");
    if (pathNode.isNull()) {
      throw new IllegalArgumentException("Parameter 'path' cannot be null");
    }

    String pathStr = pathNode.asText();
    if (pathStr == null || pathStr.trim().isEmpty()) {
      throw new IllegalArgumentException("Parameter 'path' cannot be empty");
    }

    // Validate encoding if provided
    if (parameters.has("encoding")) {
      JsonNode encodingNode = parameters.get("encoding");
      if (!encodingNode.isNull()) {
        String encoding = encodingNode.asText();
        if (encoding != null && !encoding.trim().isEmpty()) {
          try {
            Charset.forName(encoding);
          } catch (Exception e) {
            throw new IllegalArgumentException("Invalid encoding: " + encoding);
          }
        }
      }
    }
  }

  /**
   * Resolves the file path, handling both absolute and relative paths.
   * Relative paths are resolved against the current working directory.
   *
   * @param pathStr the path string to resolve
   * @return the resolved Path
   */
  private Path resolveFilePath(String pathStr) {
    Path path = Paths.get(pathStr);
    
    if (path.isAbsolute()) {
      return path;
    } else {
      // Resolve relative path against current working directory
      Path currentDir = Paths.get(System.getProperty("user.dir"));
      return currentDir.resolve(path);
    }
  }
}
