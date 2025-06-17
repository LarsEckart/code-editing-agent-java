package com.larseckart.core.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ListFilesToolTest {

  private ListFilesTool tool;
  private ObjectMapper objectMapper;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    tool = new ListFilesTool();
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("should have correct name")
  void shouldHaveCorrectName() {
    assertThat(tool.getName()).isEqualTo("list_files");
  }

  @Test
  @DisplayName("should have informative description")
  void shouldHaveInformativeDescription() {
    assertThat(tool.getDescription().toLowerCase()).contains("list");
    assertThat(tool.getDescription()).contains("directory");
  }

  @Test
  @DisplayName("should have proper parameter schema")
  void shouldHaveProperParameterSchema() {
    String schema = tool.getParameterSchema();
    assertThat(schema).contains("\"type\": \"object\"");
    assertThat(schema).contains("\"path\"");
    assertThat(schema).contains("\"show_hidden\"");
  }

  @Test
  @DisplayName("should list files in directory")
  void shouldListFilesInDirectory() throws Exception {
    // Create test files
    Files.createFile(tempDir.resolve("file1.txt"));
    Files.createFile(tempDir.resolve("file2.java"));
    Files.createDirectory(tempDir.resolve("subdir"));

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", tempDir.toString());

    String result = tool.execute(params);

    assertThat(result).contains("file1.txt");
    assertThat(result).contains("file2.java");
    assertThat(result).contains("subdir");
    assertThat(result).contains("[file]");
    assertThat(result).contains("[directory]");
  }

  @Test
  @DisplayName("should list files with sizes")
  void shouldListFilesWithSizes() throws Exception {
    Path file = tempDir.resolve("test.txt");
    Files.writeString(file, "Hello World");

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", tempDir.toString());

    String result = tool.execute(params);

    assertThat(result).contains("test.txt");
    assertThat(result).contains("11 bytes");
  }

  @Test
  @DisplayName("should sort files alphabetically")
  void shouldSortFilesAlphabetically() throws Exception {
    Files.createFile(tempDir.resolve("zebra.txt"));
    Files.createFile(tempDir.resolve("apple.txt"));
    Files.createFile(tempDir.resolve("banana.txt"));

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", tempDir.toString());

    String result = tool.execute(params);

    int appleIndex = result.indexOf("apple.txt");
    int bananaIndex = result.indexOf("banana.txt");
    int zebraIndex = result.indexOf("zebra.txt");

    assertThat(appleIndex).isLessThan(bananaIndex);
    assertThat(bananaIndex).isLessThan(zebraIndex);
  }

  @Test
  @DisplayName("should hide hidden files by default")
  void shouldHideHiddenFilesByDefault() throws Exception {
    Files.createFile(tempDir.resolve(".hidden"));
    Files.createFile(tempDir.resolve("visible.txt"));

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", tempDir.toString());

    String result = tool.execute(params);

    assertThat(result).doesNotContain(".hidden");
    assertThat(result).contains("visible.txt");
  }

  @Test
  @DisplayName("should show hidden files when requested")
  void shouldShowHiddenFilesWhenRequested() throws Exception {
    Files.createFile(tempDir.resolve(".hidden"));
    Files.createFile(tempDir.resolve("visible.txt"));

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", tempDir.toString());
    params.put("show_hidden", true);

    String result = tool.execute(params);

    assertThat(result).contains(".hidden");
    assertThat(result).contains("visible.txt");
  }

  @Test
  @DisplayName("should use current directory when path not provided")
  void shouldUseCurrentDirectoryWhenPathNotProvided() throws Exception {
    ObjectNode params = objectMapper.createObjectNode();

    String result = tool.execute(params);

    assertThat(result).contains("src");
    assertThat(result).contains("build.gradle");
  }

  @Test
  @DisplayName("should handle relative paths")
  void shouldHandleRelativePaths() throws Exception {
    Path subdir = tempDir.resolve("subdir");
    Files.createDirectory(subdir);
    Files.createFile(subdir.resolve("file.txt"));

    String currentDir = System.getProperty("user.dir");
    String relativePath = Paths.get(currentDir).relativize(subdir).toString();

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", relativePath);

    String result = tool.execute(params);

    assertThat(result).contains("file.txt");
  }

  @Test
  @DisplayName("should return error for non-existent directory")
  void shouldReturnErrorForNonExistentDirectory() throws Exception {
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", "/path/that/does/not/exist");

    String result = tool.execute(params);

    assertThat(result).contains("Error");
    assertThat(result).contains("not found");
  }

  @Test
  @DisplayName("should handle permission denied gracefully")
  void shouldHandlePermissionDeniedGracefully() throws Exception {
    // Skip this test on Windows
    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      return;
    }

    Path restrictedDir = tempDir.resolve("restricted");
    Set<PosixFilePermission> noPermissions = PosixFilePermissions.fromString("---------");
    Files.createDirectory(restrictedDir, PosixFilePermissions.asFileAttribute(noPermissions));

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", restrictedDir.toString());

    String result = tool.execute(params);

    assertThat(result).contains("Error");
    assertThat(result).containsAnyOf("Permission denied", "Access denied", restrictedDir.toString());
  }

  @Test
  @DisplayName("should format output consistently")
  void shouldFormatOutputConsistently() throws Exception {
    Files.createFile(tempDir.resolve("test.txt"));
    Files.createDirectory(tempDir.resolve("folder"));

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", tempDir.toString());

    String result = tool.execute(params);

    String[] lines = result.split("\n");
    assertThat(lines[0]).matches("Directory: .*");
    assertThat(lines[1]).isEmpty();
    
    for (int i = 2; i < lines.length; i++) {
      if (!lines[i].isEmpty()) {
        assertThat(lines[i]).matches(".*\\[(file|directory)\\].*");
      }
    }
  }

  @Test
  @DisplayName("should handle empty directories")
  void shouldHandleEmptyDirectories() throws Exception {
    Path emptyDir = tempDir.resolve("empty");
    Files.createDirectory(emptyDir);

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", emptyDir.toString());

    String result = tool.execute(params);

    assertThat(result).contains("Directory: " + emptyDir);
    assertThat(result).contains("(empty)");
  }

  @Test
  @DisplayName("should handle files with no read permissions")
  void shouldHandleFilesWithNoReadPermissions() throws Exception {
    // Skip this test on Windows
    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      return;
    }

    Path file = tempDir.resolve("no-read.txt");
    Files.createFile(file);
    Set<PosixFilePermission> writeOnly = PosixFilePermissions.fromString("-w-------");
    Files.setPosixFilePermissions(file, writeOnly);

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", tempDir.toString());

    String result = tool.execute(params);

    assertThat(result).contains("no-read.txt");
  }
}