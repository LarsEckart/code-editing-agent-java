package com.larseckart.tools.gemini;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.slf4j.Logger;

public class GeminiTools {

  private static final Logger log = getLogger(GeminiTools.class);
  private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB limit
  private static final String DEFAULT_ENCODING = "UTF-8";
  private static final int TIMEOUT_MINUTES = 1;
  private static final int MAX_OUTPUT_LENGTH = 10000;

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

  /**
   * Reads file contents from the filesystem. Supports both absolute and relative paths, various
   * encodings, and includes proper error handling and file size limits.
   *
   * @param path The file path to read (absolute or relative to current working directory)
   * @param encoding The character encoding to use (defaults to UTF-8 if null)
   * @return File contents or error message
   */
  public static String readFile(String path, String encoding) {
    log.info("Executing readFile with path: {}, encoding: {}", path, encoding);

    try {
      // Handle default values
      String pathStr = path != null ? path : ".";
      String fileEncoding = encoding != null ? encoding : DEFAULT_ENCODING;

      Path filePath = resolveFilePath(pathStr);

      // Check file size before reading
      if (Files.exists(filePath)) {
        long fileSize = Files.size(filePath);
        if (fileSize > MAX_FILE_SIZE) {
          return "Error: File is too large ("
              + fileSize
              + " bytes). Maximum supported file size is "
              + MAX_FILE_SIZE
              + " bytes.";
        }
      }

      // Read file content with specified encoding
      Charset charset = Charset.forName(fileEncoding);
      return Files.readString(filePath, charset);

    } catch (NoSuchFileException e) {
      return "Error: File not found: " + e.getFile();
    } catch (SecurityException e) {
      return "Error: Permission denied accessing file: " + path;
    } catch (IOException e) {
      return "Error: IO exception reading file: " + e.getMessage();
    } catch (IllegalArgumentException e) {
      return "Error: Invalid encoding specified: " + encoding;
    } catch (Exception e) {
      return "Error: Unexpected error reading file: " + e.getMessage();
    }
  }

  /**
   * Performs simple text replacement in files. Creates a backup before editing and validates that
   * search text exists.
   *
   * @param path The path to the file to edit
   * @param searchText The text to search for and replace
   * @param replaceText The text to replace the search text with
   * @return Success message with replacement count or error message
   */
  public static String editFile(String path, String searchText, String replaceText) {
    log.info(
        "Executing editFile with path: {}, searchText: {}, replaceText: {}",
        path,
        searchText,
        replaceText);

    try {
      // Validate required parameters
      if (path == null || path.trim().isEmpty()) {
        return "Error: 'path' parameter is required and cannot be empty";
      }
      if (searchText == null || searchText.trim().isEmpty()) {
        return "Error: 'searchText' parameter is required and cannot be empty";
      }
      if (replaceText == null) {
        return "Error: 'replaceText' parameter is required";
      }

      // Validate path for security (prevent directory traversal)
      if (path.contains("..")
          || path.startsWith("/etc/")
          || path.startsWith("/usr/")
          || path.startsWith("/bin/")) {
        log.warn("Potentially unsafe path attempted: {}", path);
        return "Error: Path not allowed for security reasons";
      }

      Path filePath;
      try {
        filePath = Paths.get(path);
        if (!filePath.isAbsolute()) {
          filePath = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        filePath = filePath.normalize();
      } catch (Exception e) {
        log.error("Invalid path: {}", path, e);
        return "Error: Invalid file path: " + path;
      }

      // Check if file exists
      if (!Files.exists(filePath)) {
        log.error("File not found: {}", filePath);
        return "Error: File not found: " + path;
      }

      // Check if it's actually a file (not a directory)
      if (!Files.isRegularFile(filePath)) {
        log.error("Path is not a regular file: {}", filePath);
        return "Error: Path is not a regular file: " + path;
      }

      // Read current file content
      String content;
      try {
        content = Files.readString(filePath);
      } catch (IOException e) {
        log.error("Failed to read file: {}", filePath, e);
        return "Error: Failed to read file: " + e.getMessage();
      }

      // Check if search text exists in the file
      if (!content.contains(searchText)) {
        log.info("Search text '{}' not found in file: {}", searchText, filePath);
        return "Error: Text '" + searchText + "' not found in file";
      }

      // Count occurrences before replacement
      int occurrences = content.split(searchText, -1).length - 1;

      // Create backup file
      Path backupPath = Paths.get(filePath + ".backup");
      try {
        Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Created backup file: {}", backupPath);
      } catch (IOException e) {
        log.error("Failed to create backup file: {}", backupPath, e);
        return "Error: Failed to create backup file: " + e.getMessage();
      }

      // Perform replacement
      String newContent = content.replace(searchText, replaceText);

      // Write updated content back to file
      try {
        Files.writeString(filePath, newContent);
        log.info("Successfully edited file: {} ({} occurrences replaced)", filePath, occurrences);
      } catch (IOException e) {
        log.error("Failed to write updated content to file: {}", filePath, e);
        return "Error: Failed to write to file: " + e.getMessage();
      }

      return String.format(
          "File edited successfully! Replaced %d occurrences of '%s' with '%s' in %s. Backup created at %s.backup",
          occurrences, searchText, replaceText, path, path);

    } catch (Exception e) {
      log.error("Unexpected error in editFile tool", e);
      return "Error: Unexpected error occurred: " + e.getMessage();
    }
  }

  /**
   * Runs all Gradle tests using gradlew with a 1-minute timeout. Provides detailed output including
   * test results, failures, and execution summary.
   *
   * @return Test execution results or error message
   */
  public static String runTests() {
    log.info("Executing runTests");

    try {

      // Ensure we're in a Gradle project
      Path currentDir = Paths.get(System.getProperty("user.dir"));
      Path gradlewScript = currentDir.resolve("gradlew");
      Path gradlewBat = currentDir.resolve("gradlew.bat");

      if (!Files.exists(gradlewScript) && !Files.exists(gradlewBat)) {
        return "Error: No gradlew script found in current directory. This tool requires a Gradle project with gradlew.";
      }

      // Build the command
      String gradlewCommand = Files.exists(gradlewScript) ? "./gradlew" : "gradlew.bat";
      ProcessBuilder processBuilder = new ProcessBuilder(gradlewCommand, "test");
      log.info("Running all tests");

      processBuilder.directory(currentDir.toFile());
      processBuilder.redirectErrorStream(true);

      // Start the process
      Process process = processBuilder.start();
      StringBuilder output = new StringBuilder();

      // Read output
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.append(line).append("\n");
          // Prevent excessive output
          if (output.length() > MAX_OUTPUT_LENGTH) {
            output.append("\n[Output truncated - too long]\n");
            break;
          }
        }
      }

      // Wait for completion with timeout
      boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);

      if (!finished) {
        process.destroyForcibly();
        return "Error: Test execution timed out after "
            + TIMEOUT_MINUTES
            + " minute.\n\nPartial output:\n"
            + output.toString();
      }

      int exitCode = process.exitValue();
      String result = output.toString();

      // Format the response
      StringBuilder response = new StringBuilder();
      response.append("Test execution completed with exit code: ").append(exitCode).append("\n\n");

      if (exitCode == 0) {
        response.append("✅ All tests passed!\n\n");
      } else {
        response.append("❌ Some tests failed or there were errors.\n\n");
      }

      response.append("Output:\n");
      response.append(result);

      return response.toString();

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return "Error: Test execution was interrupted: " + e.getMessage();
    } catch (IOException e) {
      return "Error: Failed to execute gradlew test: " + e.getMessage();
    } catch (Exception e) {
      return "Error: Unexpected error during test execution: " + e.getMessage();
    }
  }

  /**
   * Resolves the file path, handling both absolute and relative paths. Relative paths are resolved
   * against the current working directory.
   *
   * @param pathStr the path string to resolve
   * @return the resolved Path
   */
  private static Path resolveFilePath(String pathStr) {
    Path path = Paths.get(pathStr);

    if (path.isAbsolute()) {
      return path;
    } else {
      // Resolve relative path against current working directory
      Path currentDir = Paths.get(System.getProperty("user.dir"));
      return currentDir.resolve(path);
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
