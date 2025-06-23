package com.larseckart.tools.gemini;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GeminiToolsTest {

  @TempDir Path tempDir;

  @Test
  void should_list_files_in_directory() throws IOException {
    // Create test files
    Files.createFile(tempDir.resolve("file1.txt"));
    Files.createFile(tempDir.resolve("file2.java"));
    Files.createDirectory(tempDir.resolve("subdir"));

    String result = GeminiTools.listFiles(tempDir.toString(), false);

    assertTrue(result.contains("Directory: " + tempDir.toAbsolutePath()));
    assertTrue(result.contains("file1.txt [file]"));
    assertTrue(result.contains("file2.java [file]"));
    assertTrue(result.contains("subdir [directory]"));
  }

  @Test
  void should_use_current_directory_when_path_is_null() {
    String result = GeminiTools.listFiles(null, false);

    assertNotNull(result);
    assertTrue(result.startsWith("Directory: "));
  }

  @Test
  void should_hide_hidden_files_by_default() throws IOException {
    Files.createFile(tempDir.resolve(".hidden"));
    Files.createFile(tempDir.resolve("visible.txt"));

    String result = GeminiTools.listFiles(tempDir.toString(), false);

    assertFalse(result.contains(".hidden"));
    assertTrue(result.contains("visible.txt"));
  }

  @Test
  void should_show_hidden_files_when_requested() throws IOException {
    Files.createFile(tempDir.resolve(".hidden"));
    Files.createFile(tempDir.resolve("visible.txt"));

    String result = GeminiTools.listFiles(tempDir.toString(), true);

    assertTrue(result.contains(".hidden"));
    assertTrue(result.contains("visible.txt"));
  }

  @Test
  void should_handle_null_showHidden_parameter() throws IOException {
    Files.createFile(tempDir.resolve(".hidden"));
    Files.createFile(tempDir.resolve("visible.txt"));

    String result = GeminiTools.listFiles(tempDir.toString(), null);

    // null should default to false
    assertFalse(result.contains(".hidden"));
    assertTrue(result.contains("visible.txt"));
  }

  @Test
  void should_return_error_for_non_existent_directory() {
    String result = GeminiTools.listFiles("/path/that/does/not/exist", false);

    assertTrue(result.startsWith("Error: Directory not found:"));
  }

  @Test
  void should_return_error_for_file_instead_of_directory() throws IOException {
    Path file = tempDir.resolve("file.txt");
    Files.createFile(file);

    String result = GeminiTools.listFiles(file.toString(), false);

    assertTrue(result.startsWith("Error: Path is not a directory:"));
  }

  @Test
  void should_show_empty_for_empty_directory() {
    String result = GeminiTools.listFiles(tempDir.toString(), false);

    assertTrue(result.contains("(empty)"));
  }

  @Test
  void should_format_file_sizes() throws IOException {
    Path smallFile = tempDir.resolve("small.txt");
    Path mediumFile = tempDir.resolve("medium.txt");

    Files.write(smallFile, new byte[100]);
    Files.write(mediumFile, new byte[2048]);

    String result = GeminiTools.listFiles(tempDir.toString(), false);

    assertTrue(result.contains("small.txt [file] - 100 bytes"));
    assertTrue(result.contains("medium.txt [file] - 2.0 KB"));
  }
}
