package com.larseckart.core.tools;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.JsonNode;
import com.larseckart.core.domain.Tool;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
 * A tool that runs all Gradle tests using gradlew with a 1-minute timeout. Provides detailed output
 * including test results, failures, and execution summary.
 */
public class RunTestsTool implements Tool {

  private static final Logger log = getLogger(RunTestsTool.class);
  private static final int TIMEOUT_MINUTES = 1;
  private static final int MAX_OUTPUT_LENGTH = 10000;

  @Override
  public String getName() {
    return "run_tests";
  }

  @Override
  public String getDescription() {
    return "Runs all Gradle tests using gradlew. "
        + "Provides detailed output including test results, failures, and execution summary.";
  }

  @Override
  public String getParameterSchema() {
    return """
      {
        "$schema": "https://json-schema.org/draft/2020-12/schema",
        "type": "object",
        "properties": {},
        "additionalProperties": false
      }""";
  }

  @Override
  public String execute(JsonNode parameters) {
    log.info("Executing RunTestsTool with parameters: {}", parameters);

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

  @Override
  public void validate(JsonNode parameters) {
    // No parameters to validate
  }
}
