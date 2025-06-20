package com.larseckart.core.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ListFilesToolTest {

  private ListFilesTool tool;
  private ObjectMapper objectMapper;

  @TempDir Path tempDir;

  @BeforeEach
  void setUp() {
    tool = new ListFilesTool();
    objectMapper = new ObjectMapper();
  }

  @Test
  void should_have_correct_name() {
    assertThat(tool.getName()).isEqualTo("list_files");
  }

  @Test
  void should_have_informative_description() {
    assertThat(tool.getDescription().toLowerCase()).contains("list");
    assertThat(tool.getDescription()).contains("directory");
  }

  @Test
  void should_have_proper_parameter_schema() {
    String schema = tool.getParameterSchema();
    assertThat(schema).contains("\"type\": \"object\"");
    assertThat(schema).contains("\"path\"");
    assertThat(schema).contains("\"show_hidden\"");
  }

  @Test
  void should_list_files_in_directory() throws Exception {
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
  void should_list_files_with_sizes() throws Exception {
    Path file = tempDir.resolve("test.txt");
    Files.writeString(file, "Hello World");

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", tempDir.toString());

    String result = tool.execute(params);

    assertThat(result).contains("test.txt");
    assertThat(result).contains("11 bytes");
  }

  @Test
  void should_sort_files_alphabetically() throws Exception {
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
  void should_hide_hidden_files_by_default() throws Exception {
    Files.createFile(tempDir.resolve(".hidden"));
    Files.createFile(tempDir.resolve("visible.txt"));

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", tempDir.toString());

    String result = tool.execute(params);

    assertThat(result).doesNotContain(".hidden");
    assertThat(result).contains("visible.txt");
  }

  @Test
  void should_show_hidden_files_when_requested() throws Exception {
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
  void should_use_current_directory_when_path_not_provided() throws Exception {
    ObjectNode params = objectMapper.createObjectNode();

    String result = tool.execute(params);

    assertThat(result).contains("src");
    assertThat(result).contains("build.gradle");
  }

  @Test
  void should_handle_relative_paths() throws Exception {
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
  void should_return_error_for_non_existent_directory() throws Exception {
    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", "/path/that/does/not/exist");

    String result = tool.execute(params);

    assertThat(result).contains("Error");
    assertThat(result).contains("not found");
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  void should_handle_permission_denied_gracefully() throws Exception {

    Path restrictedDir = tempDir.resolve("restricted");
    Set<PosixFilePermission> noPermissions = PosixFilePermissions.fromString("---------");
    Files.createDirectory(restrictedDir, PosixFilePermissions.asFileAttribute(noPermissions));

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", restrictedDir.toString());

    String result = tool.execute(params);

    assertThat(result).contains("Error");
    assertThat(result)
        .containsAnyOf("Permission denied", "Access denied", restrictedDir.toString());
  }

  @Test
  void should_format_output_consistently() throws Exception {
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
  void should_handle_empty_directories() throws Exception {
    Path emptyDir = tempDir.resolve("empty");
    Files.createDirectory(emptyDir);

    ObjectNode params = objectMapper.createObjectNode();
    params.put("path", emptyDir.toString());

    String result = tool.execute(params);

    assertThat(result).contains("Directory: " + emptyDir);
    assertThat(result).contains("(empty)");
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  void should_handle_files_with_no_read_permissions() throws Exception {

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
