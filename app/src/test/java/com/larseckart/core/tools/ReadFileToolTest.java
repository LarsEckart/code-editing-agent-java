package com.larseckart.core.tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
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
    void should_have_correct_name() {
        assertThat(readFileTool.getName()).isEqualTo("read_file");
    }

    @Test
    void should_have_correct_description() {
        String description = readFileTool.getDescription();
        assertThat(description).isNotNull();
        assertThat(description.toLowerCase()).contains("read");
        assertThat(description.toLowerCase()).contains("file");
        assertThat(description.toLowerCase()).contains("filesystem");
    }

    @Test
    void should_have_valid_parameter_schema() throws Exception {
        String schema = readFileTool.getParameterSchema();
        assertThat(schema).isNotNull();
        assertThat(schema.trim()).isNotEmpty();

        JsonNode schemaNode = objectMapper.readTree(schema);
        assertThat(schemaNode.get("type").asText()).isEqualTo("object");

        JsonNode properties = schemaNode.get("properties");
        assertThat(properties).isNotNull();
        assertThat(properties.has("path")).isTrue();
        assertThat(properties.get("path").get("type").asText()).isEqualTo("string");

        // Encoding should be optional
        if (properties.has("encoding")) {
            assertThat(properties.get("encoding").get("type").asText()).isEqualTo("string");
        }

        JsonNode required = schemaNode.get("required");
        assertThat(required).isNotNull();
        assertThat(required.isArray()).isTrue();
        assertThat(required.get(0).asText()).isEqualTo("path");
    }

    @Test
    void should_read_file_with_absolute_path() throws Exception {
        // Create test file
        Path testFile = tempDir.resolve("test.txt");
        String content = "Hello, World!";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        ObjectNode params = objectMapper.createObjectNode();
        params.put("path", testFile.toAbsolutePath().toString());

        String result = readFileTool.execute(params);
        assertThat(result).isEqualTo(content);
    }

    @Test
    void should_read_file_with_relative_path() throws Exception {
        // Create test file in current working directory
        Path currentDir = Path.of(System.getProperty("user.dir"));
        Path testFile = currentDir.resolve("test-relative.txt");
        String content = "Relative path test";

        try {
            Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

            ObjectNode params = objectMapper.createObjectNode();
            params.put("path", "test-relative.txt");

            String result = readFileTool.execute(params);
            assertThat(result).isEqualTo(content);
        } finally {
            // Clean up
            Files.deleteIfExists(testFile);
        }
    }

    @Test
    void should_read_file_with_different_encodings() throws Exception {
        Path testFile = tempDir.resolve("encoded.txt");
        String content = "Café naïve résumé";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        ObjectNode params = objectMapper.createObjectNode();
        params.put("path", testFile.toAbsolutePath().toString());
        params.put("encoding", "UTF-8");

        String result = readFileTool.execute(params);
        assertThat(result).isEqualTo(content);
    }

    @Test
    void should_use_default_encoding_when_not_specified() throws Exception {
        Path testFile = tempDir.resolve("default-encoding.txt");
        String content = "Default encoding test";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        ObjectNode params = objectMapper.createObjectNode();
        params.put("path", testFile.toAbsolutePath().toString());

        String result = readFileTool.execute(params);
        assertThat(result).isEqualTo(content);
    }

    @Test
    void should_throw_exception_for_non_existent_file() {
        ObjectNode params = objectMapper.createObjectNode();
        params.put("path", "/non/existent/file.txt");

        String result = readFileTool.execute(params);
        assertThat(result).contains("Error");
        assertThat(result.contains("not found") || result.contains("No such file")).isTrue();
    }

    @Test
    void should_handle_permission_denied() throws Exception {
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
            assertThat(result.contains("Error") || result.contains("Permission denied")).isTrue();
        } catch (UnsupportedOperationException e) {
            // Skip this test on systems that don't support POSIX permissions
            assumeTrue(false, "POSIX permissions not supported on this system");
        }
    }

    @Test
    void should_enforce_file_size_limit() throws Exception {
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
        assertThat(result.contains("Error") || result.contains("too large") || result.contains("size limit")).isTrue();
    }

    @Test
    void should_handle_empty_file() throws Exception {
        Path testFile = tempDir.resolve("empty.txt");
        Files.createFile(testFile);

        ObjectNode params = objectMapper.createObjectNode();
        params.put("path", testFile.toAbsolutePath().toString());

        String result = readFileTool.execute(params);
        assertThat(result).isEqualTo("");
    }

    @Test
    void should_validate_required_parameters() {
        ObjectNode params = objectMapper.createObjectNode();
        // Missing required "path" parameter

        assertThatThrownBy(() -> readFileTool.validate(params)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_validate_null_parameters() {
        assertThatThrownBy(() -> readFileTool.validate(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> readFileTool.execute(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_validate_empty_path() {
        ObjectNode params = objectMapper.createObjectNode();
        params.put("path", "");

        assertThatThrownBy(() -> readFileTool.validate(params)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_validate_null_path() {
        ObjectNode params = objectMapper.createObjectNode();
        params.putNull("path");

        assertThatThrownBy(() -> readFileTool.validate(params)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_accept_valid_parameters() {
        ObjectNode params = objectMapper.createObjectNode();
        params.put("path", "/some/valid/path.txt");

        assertThatCode(() -> readFileTool.validate(params)).doesNotThrowAnyException();
    }

    @Test
    void should_accept_valid_parameters_with_encoding() {
        ObjectNode params = objectMapper.createObjectNode();
        params.put("path", "/some/valid/path.txt");
        params.put("encoding", "UTF-8");

        assertThatCode(() -> readFileTool.validate(params)).doesNotThrowAnyException();
    }

    @Test
    void should_read_file_with_multiple_lines() throws Exception {
        Path testFile = tempDir.resolve("multiline.txt");
        String content = "Line 1\nLine 2\nLine 3\n";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        ObjectNode params = objectMapper.createObjectNode();
        params.put("path", testFile.toAbsolutePath().toString());

        String result = readFileTool.execute(params);
        assertThat(result).isEqualTo(content);
    }

    @Test
    void should_read_file_with_special_characters() throws Exception {
        Path testFile = tempDir.resolve("special.txt");
        String content = "Special chars: !@#$%^&*()_+-={}[]|\\:;\"'<>?,./";
        Files.write(testFile, content.getBytes(StandardCharsets.UTF_8));

        ObjectNode params = objectMapper.createObjectNode();
        params.put("path", testFile.toAbsolutePath().toString());

        String result = readFileTool.execute(params);
        assertThat(result).isEqualTo(content);
    }

    private void assumeTrue(boolean condition, String message) {
        if (!condition) {
            Assumptions.assumeTrue(false, message);
        }
    }
}
