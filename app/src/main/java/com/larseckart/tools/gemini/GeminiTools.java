package com.larseckart.tools.gemini;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;
import org.slf4j.Logger;

public class GeminiTools {

  private static final Logger log = getLogger(GeminiTools.class);

  /**
   * Lists the contents of a directory, including files and subdirectories
   *
   * @param path The directory path to list. Defaults to current directory if not provided
   * @param showHidden Whether to show hidden files (files starting with dot). Defaults to false
   * @return Directory listing or error message
   */
  public static String listFiles(String path, Boolean showHidden) {
    log.info("Executing listFiles with path: {}, showHidden: {}", path, showHidden);

    try {
      // Handle default values
      String pathStr = path != null ? path : ".";
      boolean includeHidden = showHidden != null ? showHidden : false;

      Path dirPath = Paths.get(pathStr).normalize();

      // Validate path exists
      if (!Files.exists(dirPath)) {
        return "Error: Directory not found: " + dirPath;
      }

      // Validate path is a directory
      if (!Files.isDirectory(dirPath)) {
        return "Error: Path is not a directory: " + dirPath;
      }

      StringBuilder result = new StringBuilder();
      result.append("Directory: ").append(dirPath.toAbsolutePath()).append("\n\n");

      try (Stream<Path> files = Files.list(dirPath)) {
        var fileList =
            files
                .filter(p -> includeHidden || !p.getFileName().toString().startsWith("."))
                .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                .toList();

        if (fileList.isEmpty()) {
          result.append("(empty)");
        } else {
          for (Path file : fileList) {
            String fileName = file.getFileName().toString();
            String type = Files.isDirectory(file) ? "[directory]" : "[file]";

            result.append(fileName).append(" ").append(type);

            // Add file size for regular files
            if (Files.isRegularFile(file)) {
              try {
                long size = Files.size(file);
                result.append(" - ").append(formatFileSize(size));
              } catch (IOException e) {
                // Can't read size, skip it
                log.debug("Could not read size for file: {}", file, e);
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

  private static String formatFileSize(long bytes) {
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
