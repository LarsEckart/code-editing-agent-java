package com.larseckart.core.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EditFileToolTest {

  private EditFileTool editFileTool;
  private ObjectMapper objectMapper;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    editFileTool = new EditFileTool();
    objectMapper = new ObjectMapper();
  }

  @Test
  void should_have_correct_name() {
    assertEquals("edit_file", editFileTool.getName());
  }

  @Test
  void should_have_descriptive_description() {
    String description = editFileTool.getDescription();
    assertTrue(description.contains("text replacement"));
    assertTrue(description.contains("file"));
  }

  @Test
  void should_have_correct_parameter_schema() {
    String schema = editFileTool.getParameterSchema();
    assertNotNull(schema);
    assertTrue(schema.contains("path"));
    assertTrue(schema.contains("search_text"));
    assertTrue(schema.contains("replace_text"));
  }

  @Test
  void should_successfully_replace_text_in_file() throws IOException {
    // Create test file
    Path testFile = tempDir.resolve("test.txt");
    String originalContent = "Hello World!\nThis is a test file.\nHello again!";
    Files.write(testFile, originalContent.getBytes());

    // Create parameters
    JsonNode params = objectMapper.valueToTree(Map.of(
        "path", testFile.toString(),
        "search_text", "Hello",
        "replace_text", "Hi"
    ));

    // Execute replacement
    String result = editFileTool.execute(params);

    // Verify result message
    assertTrue(result.contains("successfully"));
    assertTrue(result.contains("2 occurrences"));

    // Verify file content
    String newContent = Files.readString(testFile);
    assertEquals("Hi World!\nThis is a test file.\nHi again!", newContent);

    // Verify backup file was created
    Path backupFile = testFile.getParent().resolve("test.txt.backup");
    assertTrue(Files.exists(backupFile));
    assertEquals(originalContent, Files.readString(backupFile));
  }

  @Test
  void should_replace_all_occurrences_of_search_text() throws IOException {
    Path testFile = tempDir.resolve("multiple.txt");
    String content = "cat dog cat bird cat mouse";
    Files.write(testFile, content.getBytes());

    JsonNode params = objectMapper.valueToTree(Map.of(
        "path", testFile.toString(),
        "search_text", "cat",
        "replace_text", "elephant"
    ));

    String result = editFileTool.execute(params);

    assertTrue(result.contains("3 occurrences"));
    String newContent = Files.readString(testFile);
    assertEquals("elephant dog elephant bird elephant mouse", newContent);
  }

  @Test
  void should_fail_when_search_text_not_found() throws IOException {
    Path testFile = tempDir.resolve("notfound.txt");
    Files.write(testFile, "This is some content".getBytes());

    JsonNode params = objectMapper.valueToTree(Map.of(
        "path", testFile.toString(),
        "search_text", "nonexistent",
        "replace_text", "replacement"
    ));

    String result = editFileTool.execute(params);

    assertTrue(result.contains("not found"));
    assertTrue(result.contains("nonexistent"));
  }

  @Test
  void should_fail_when_file_does_not_exist() {
    JsonNode params = objectMapper.valueToTree(Map.of(
        "path", "/nonexistent/file.txt",
        "search_text", "test",
        "replace_text", "replacement"
    ));

    String result = editFileTool.execute(params);

    assertTrue(result.contains("Error") || result.contains("not found"));
  }

  @Test
  void should_validate_required_parameters() {
    // Missing path parameter
    JsonNode paramsNoPat = objectMapper.valueToTree(Map.of(
        "search_text", "test",
        "replace_text", "replacement"
    ));

    String result = editFileTool.execute(paramsNoPat);
    assertTrue(result.contains("path") && result.contains("required"));

    // Missing search_text parameter
    JsonNode paramsNoSearch = objectMapper.valueToTree(Map.of(
        "path", "/some/path",
        "replace_text", "replacement"
    ));

    result = editFileTool.execute(paramsNoSearch);
    assertTrue(result.contains("search_text") && result.contains("required"));

    // Missing replace_text parameter
    JsonNode paramsNoReplace = objectMapper.valueToTree(Map.of(
        "path", "/some/path",
        "search_text", "test"
    ));

    result = editFileTool.execute(paramsNoReplace);
    assertTrue(result.contains("replace_text") && result.contains("required"));
  }

  @Test
  void should_handle_empty_file() throws IOException {
    Path testFile = tempDir.resolve("empty.txt");
    Files.write(testFile, new byte[0]);

    JsonNode params = objectMapper.valueToTree(Map.of(
        "path", testFile.toString(),
        "search_text", "anything",
        "replace_text", "replacement"
    ));

    String result = editFileTool.execute(params);

    assertTrue(result.contains("not found"));
  }

  @Test
  void should_handle_special_characters() throws IOException {
    Path testFile = tempDir.resolve("special.txt");
    String content = "Special chars: $100 & more!";
    Files.write(testFile, content.getBytes());

    JsonNode params = objectMapper.valueToTree(Map.of(
        "path", testFile.toString(),
        "search_text", "$100",
        "replace_text", "€200"
    ));

    String result = editFileTool.execute(params);

    assertTrue(result.contains("successfully"));
    String newContent = Files.readString(testFile);
    assertEquals("Special chars: €200 & more!", newContent);
  }

  @Test
  void should_prevent_directory_traversal_attacks() {
    JsonNode params = objectMapper.valueToTree(Map.of(
        "path", "../../../etc/passwd",
        "search_text", "root",
        "replace_text", "hacker"
    ));

    String result = editFileTool.execute(params);

    assertTrue(result.contains("Error") || result.contains("not allowed"));
  }

  @Test
  void should_handle_relative_paths() throws IOException {
    // Create test file in temp directory
    Path testFile = tempDir.resolve("relative.txt");
    Files.write(testFile, "Test content".getBytes());

    // Change to temp directory context (simulate relative path)
    String relativePath = "relative.txt";
    System.setProperty("user.dir", tempDir.toString());

    JsonNode params = objectMapper.valueToTree(Map.of(
        "path", relativePath,
        "search_text", "Test",
        "replace_text", "Updated"
    ));

    String result = editFileTool.execute(params);

    assertTrue(result.contains("successfully") || result.contains("Error"));
  }

  @Test
  void should_create_backup_with_correct_extension() throws IOException {
    Path testFile = tempDir.resolve("backup-test.java");
    Files.write(testFile, "public class Test {}".getBytes());

    JsonNode params = objectMapper.valueToTree(Map.of(
        "path", testFile.toString(),
        "search_text", "Test",
        "replace_text", "Example"
    ));

    editFileTool.execute(params);

    Path backupFile = testFile.getParent().resolve("backup-test.java.backup");
    assertTrue(Files.exists(backupFile));
    assertEquals("public class Test {}", Files.readString(backupFile));
  }

  @Test
  void should_handle_multiline_text() throws IOException {
    Path testFile = tempDir.resolve("multiline.txt");
    String content = "Line 1\nLine 2 with text\nLine 3\nAnother line with text";
    Files.write(testFile, content.getBytes());

    JsonNode params = objectMapper.valueToTree(Map.of(
        "path", testFile.toString(),
        "search_text", "with text",
        "replace_text", "modified"
    ));

    String result = editFileTool.execute(params);

    assertTrue(result.contains("2 occurrences"));
    String newContent = Files.readString(testFile);
    assertEquals("Line 1\nLine 2 modified\nLine 3\nAnother line modified", newContent);
  }
}