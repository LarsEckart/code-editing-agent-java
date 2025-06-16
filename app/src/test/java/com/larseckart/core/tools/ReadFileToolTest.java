package com.larseckart.core.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReadFileToolTest {

  private ReadFileTool readFileTool;
  private ObjectMapper objectMapper;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    readFileTool = new ReadFileTool();
    objectMapper = new ObjectMapper();
  }

  @Test
  void shouldHaveCorrectName() {
    assertEquals("read_file", readFileTool.getName());
  }

  @Test
  void shouldHaveCorrectDescription() {
    String description = readFileTool.getDescription();
    assertNotNull(description);
    assertTrue(description.toLowerCase().contains("read"));
    assertTrue(description.toLowerCase().contains("file"));
    assertTrue(description.toLowerCase().contains("filesystem"));
  }

  @Test
  void shouldHaveValidParameterSchema() throws Exception {
    String schema = readFileTool.getParameterSchema();
    assertNotNull(schema);
    assertFalse(schema.trim().isEmpty());
    
    JsonNode schemaNode = objectMapper.readTree(schema);
    assertEquals("object", schemaNode.get("type").asText());
    
    JsonNode properties = schemaNode.get("properties");
    assertNotNull(properties);
    assertTrue(properties.has("path"));
    assertEquals("string", properties.get("path").get("type").asText());
    
    // Encoding should be optional
    if (properties.has("encoding")) {
      assertEquals("string", properties.get("encoding").get("type").asText());
    }
    
    JsonNode required = schemaNode.get("required");
    assertNotNull(required);
    assertTrue(required.isArray());
    assertEquals("path", required.get(0).asText());
  }

  @Test
  void shouldReadFileWithAbsolutePath() throws Exception {
    // Create test file
    Path testFile = tempDir.resolve("test.txt");
    String content = "Hello, World!";
    Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));
    
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", testFile.toAbsolutePath().toString());
    
    String result = readFileTool.execute(params);
    assertEquals(content, result);
  }

  @Test
  void shouldReadFileWithRelativePath() throws Exception {
    // Create test file in current working directory
    Path currentDir = Path.of(System.getProperty("user.dir"));
    Path testFile = currentDir.resolve("test-relative.txt");
    String content = "Relative path test";
    
    try {
      Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));
      
      ObjectNode params = objectMapper.createObjectNode();
      params.put("path", "test-relative.txt");
      
      String result = readFileTool.execute(params);
      assertEquals(content, result);
    } finally {
      // Clean up
      Files.deleteIfExists(testFile);
    }
  }

  @Test
  void shouldReadFileWithDifferentEncodings() throws Exception {
    Path testFile = tempDir.resolve("encoded.txt");
    String content = "Café naïve résumé";
    Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));
    
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", testFile.toAbsolutePath().toString());
    params.put("encoding", "UTF-8");
    
    String result = readFileTool.execute(params);
    assertEquals(content, result);
  }

  @Test
  void shouldUseDefaultEncodingWhenNotSpecified() throws Exception {
    Path testFile = tempDir.resolve("default-encoding.txt");
    String content = "Default encoding test";
    Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));
    
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", testFile.toAbsolutePath().toString());
    
    String result = readFileTool.execute(params);
    assertEquals(content, result);
  }

  @Test
  void shouldThrowExceptionForNonExistentFile() {
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", "/non/existent/file.txt");
    
    String result = readFileTool.execute(params);
    assertTrue(result.contains("Error"));
    assertTrue(result.contains("not found") || result.contains("No such file"));
  }

  @Test
  void shouldHandlePermissionDenied() throws Exception {
    // This test might not work on all systems, so we'll make it conditional
    Path testFile = tempDir.resolve("no-permission.txt");
    Files.write(testFile, "secret content".getBytes(StandardCharsets.UTF_8));
    
    try {
      // Try to remove read permissions (Unix-like systems only)
      Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(testFile);
      permissions.remove(PosixFilePermission.OWNER_READ);
      permissions.remove(PosixFilePermission.GROUP_READ);
      permissions.remove(PosixFilePermission.OTHERS_READ);
      Files.setPosixFilePermissions(testFile, permissions);
      
      ObjectNode params = objectMapper.createObjectNode();
      params.put("path", testFile.toAbsolutePath().toString());
      
      String result = readFileTool.execute(params);
      assertTrue(result.contains("Error") || result.contains("Permission denied"));
    } catch (UnsupportedOperationException e) {
      // Skip this test on systems that don't support POSIX permissions
      assumeTrue(false, "POSIX permissions not supported on this system");
    }
  }

  @Test
  void shouldEnforceFileSizeLimit() throws Exception {
    Path testFile = tempDir.resolve("large.txt");
    StringBuilder largeContent = new StringBuilder();
    
    // Create content larger than 1MB
    for (int i = 0; i < 200000; i++) {
      largeContent.append("This is a line of text to make a large file.\n");
    }
    
    Files.write(testFile, largeContent.toString().getBytes(StandardCharsets.UTF_8));
    
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", testFile.toAbsolutePath().toString());
    
    String result = readFileTool.execute(params);
    assertTrue(result.contains("Error") || result.contains("too large") || result.contains("size limit"));
  }

  @Test
  void shouldHandleEmptyFile() throws Exception {
    Path testFile = tempDir.resolve("empty.txt");
    Files.createFile(testFile);
    
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", testFile.toAbsolutePath().toString());
    
    String result = readFileTool.execute(params);
    assertEquals("", result);
  }

  @Test
  void shouldValidateRequiredParameters() {
    ObjectNode params = objectMapper.createObjectNode();
    // Missing required "path" parameter
    
    assertThrows(IllegalArgumentException.class, () -> readFileTool.validate(params));
  }

  @Test
  void shouldValidateNullParameters() {
    assertThrows(IllegalArgumentException.class, () -> readFileTool.validate(null));
    assertThrows(IllegalArgumentException.class, () -> readFileTool.execute(null));
  }

  @Test
  void shouldValidateEmptyPath() {
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", "");
    
    assertThrows(IllegalArgumentException.class, () -> readFileTool.validate(params));
  }

  @Test
  void shouldValidateNullPath() {
    ObjectNode params = objectMapper.createObjectNode();
    params.putNull("path");
    
    assertThrows(IllegalArgumentException.class, () -> readFileTool.validate(params));
  }

  @Test
  void shouldAcceptValidParameters() {
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", "/some/valid/path.txt");
    
    assertDoesNotThrow(() -> readFileTool.validate(params));
  }

  @Test
  void shouldAcceptValidParametersWithEncoding() {
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", "/some/valid/path.txt");
    params.put("encoding", "UTF-8");
    
    assertDoesNotThrow(() -> readFileTool.validate(params));
  }

  @Test
  void shouldReadFileWithMultipleLines() throws Exception {
    Path testFile = tempDir.resolve("multiline.txt");
    String content = "Line 1\nLine 2\nLine 3\n";
    Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));
    
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", testFile.toAbsolutePath().toString());
    
    String result = readFileTool.execute(params);
    assertEquals(content, result);
  }

  @Test
  void shouldReadFileWithSpecialCharacters() throws Exception {
    Path testFile = tempDir.resolve("special.txt");
    String content = "Special chars: !@#$%^&*()_+-={}[]|\\:;\"'<>?,./";
    Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));
    
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", testFile.toAbsolutePath().toString());
    
    String result = readFileTool.execute(params);
    assertEquals(content, result);
  }

  private void assumeTrue(boolean condition, String message) {
    if (!condition) {
      Assumptions.assumeTrue(false, message);
    }
  }
}