package com.larseckart.core.tools;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.JsonNode;
import com.larseckart.core.domain.Tool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;

public class ListFilesTool implements Tool {

  private static final Logger log = getLogger(ListFilesTool.class);

  @Override
  public String getName() {
    return "list_files";
  }

  @Override
  public String getDescription() {
    return "Lists the contents of a directory, including files and subdirectories";
  }

  @Override
  public String getParameterSchema() {
    return """
      {
        "type": "object",
        "properties": {
          "path": {
            "type": "string",
            "description": "The directory path to list. Defaults to current directory if not provided"
          },
          "show_hidden": {
            "type": "boolean",
            "description": "Whether to show hidden files (files starting with dot). Defaults to false"
          }
        }
      }
      """;
  }

  @Override
  public void validate(JsonNode parameters) {
    if (parameters == null) {
      throw new IllegalArgumentException("Parameters cannot be null");
    }

    if (parameters.has("path") && !parameters.get("path").isTextual()) {
      throw new IllegalArgumentException("Parameter 'path' must be a string");
    }

    if (parameters.has("show_hidden") && !parameters.get("show_hidden").isBoolean()) {
      throw new IllegalArgumentException("Parameter 'show_hidden' must be a boolean");
    }
  }

  @Override
  public String execute(JsonNode parameters) {
    log.info("Executing ListFilesTool with parameters: {}", parameters);
    try {
      String pathStr = parameters.has("path") ? parameters.get("path").asText() : ".";
      boolean showHidden =
          parameters.has("show_hidden") && parameters.get("show_hidden").asBoolean();

      Path path = Paths.get(pathStr).normalize();

      if (!Files.exists(path)) {
        return "Error: Directory not found: " + path;
      }

      if (!Files.isDirectory(path)) {
        return "Error: Path is not a directory: " + path;
      }

      StringBuilder result = new StringBuilder();
      result.append("Directory: ").append(path.toAbsolutePath()).append("\n\n");

      try (Stream<Path> files = Files.list(path)) {
        var fileList =
            files
                .filter(p -> showHidden || !p.getFileName().toString().startsWith("."))
                .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                .toList();

        if (fileList.isEmpty()) {
          result.append("(empty)");
        } else {
          for (Path file : fileList) {
            String fileName = file.getFileName().toString();
            String type = Files.isDirectory(file) ? "[directory]" : "[file]";

            result.append(fileName).append(" ").append(type);

            if (Files.isRegularFile(file)) {
              try {
                long size = Files.size(file);
                result.append(" - ").append(formatSize(size));
              } catch (IOException e) {
                // Can't read size, skip it
              }
            }

            result.append("\n");
          }
        }
      }

      return result.toString();
    } catch (IOException e) {
      if (e.getMessage() != null && e.getMessage().contains("ermission")) {
        return "Error: Permission denied - " + e.getMessage();
      }
      return "Error: " + e.getMessage();
    } catch (SecurityException e) {
      return "Error: Permission denied - " + e.getMessage();
    }
  }

  private String formatSize(long bytes) {
    if (bytes < 1024) {
      return bytes + " bytes";
    } else if (bytes < 1024 * 1024) {
      return String.format("%.1f KB", bytes / 1024.0);
    } else if (bytes < 1024 * 1024 * 1024) {
      return String.format("%.1f MB", bytes / (1024.0 * 1024));
    } else {
      return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
  }
}
