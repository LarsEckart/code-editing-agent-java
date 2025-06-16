# Detailed Implementation Prompts for Java AI Agent Tool System

**CRITICAL: Test-Driven Development (TDD) Required**
Every step must follow strict TDD principles:
1. **Write the test first** - Create comprehensive unit tests before any implementation
2. **See it fail** - Run tests to confirm they fail for expected reasons
3. **Implement to pass** - Write minimal code to make tests pass
4. **Refactor** - Clean up while keeping tests green
5. **Repeat** - No code is written without a failing test first

## Phase 1: Core Tool Architecture

### Step 1: Create Tool Interface
**Prompt:** 
1. **FIRST**: Create test file `src/test/java/com/larseckart/core/domain/ToolTest.java` with tests for:
   - Tool interface contract requirements
   - Mock implementations to verify method signatures
   - Parameter validation behavior expectations
   - Error handling scenarios
2. **RUN TESTS**: Execute `./gradlew test --tests "*ToolTest*"` and confirm they fail
3. **THEN**: Create `Tool` interface in `core/domain/Tool.java` implementing exactly what tests expect:
   - A `getName()` method returning a String for the tool name
   - A `getDescription()` method returning a String for tool description
   - A `getParameterSchema()` method returning a JSON schema (as String or JsonNode) for parameter validation
   - An `execute(JsonNode parameters)` method that takes parameters and returns a String result
   - Consider adding a `validate(JsonNode parameters)` method for parameter validation
   - Use proper Java conventions and follow the existing codebase patterns from `core/domain/`
   - Add appropriate JavaDoc comments explaining the interface contract
4. **VERIFY**: Run tests again to ensure they pass

### Step 2: Build Tool Registry
**Prompt:**
1. **FIRST**: Create test file `src/test/java/com/larseckart/core/services/ToolRegistryTest.java` with tests for:
   - Tool registration and retrieval functionality
   - Map-based storage behavior
   - Error handling for unknown tools
   - Claude function definition conversion
   - Function call routing
   - Spring Boot compatibility
2. **RUN TESTS**: Execute `./gradlew test --tests "*ToolRegistryTest*"` and confirm they fail
3. **THEN**: Create `ToolRegistry` service class in `core/services/ToolRegistry.java` implementing exactly what tests expect:
   - Store a collection of Tool instances (use Map<String, Tool> for name-based lookup)
   - Provide `registerTool(Tool tool)` method to add tools
   - Provide `getTool(String name)` method to retrieve tools
   - Provide `getAllTools()` method to get all registered tools
   - Include `convertToClaudeFunctionDefinitions()` method that transforms tools into the format expected by Claude API (reference the Anthropic Java SDK documentation)
   - Add `routeFunctionCall(String toolName, JsonNode parameters)` method to execute tools
   - Follow the service patterns from existing `ConversationService.java`
   - Include proper error handling for unknown tools
   - Use dependency injection annotations if needed for Spring Boot compatibility
4. **VERIFY**: Run tests again to ensure they pass

## Phase 2: File Manipulation Tools

### Step 3: Implement ReadFileTool
**Prompt:**
1. **FIRST**: Create test file `src/test/java/com/larseckart/core/tools/ReadFileToolTest.java` with tests for:
   - Successful file reading with various encodings
   - Relative and absolute path handling
   - File not found scenarios
   - Permission denied scenarios
   - File size limit enforcement
   - IO exception handling
   - Parameter validation
   - Tool interface contract compliance
2. **SETUP**: Create test files in `src/test/resources/` for testing scenarios
3. **RUN TESTS**: Execute `./gradlew test --tests "*ReadFileToolTest*"` and confirm they fail
4. **THEN**: Create `ReadFileTool` class in `core/tools/ReadFileTool.java` implementing exactly what tests expect:
   - Tool name should be "read_file"
   - Description should explain it reads file contents from the filesystem
   - Parameter schema should require a "path" field (string type) with optional "encoding" field (default UTF-8)
   - Execute method should read file contents and return as string
   - Handle both relative paths (relative to current working directory) and absolute paths
   - Include comprehensive error handling for file not found, permission denied, and IO exceptions
   - Return meaningful error messages that Claude can understand and relay to users
   - Consider file size limits to prevent memory issues (maybe 1MB max)
   - Follow Java NIO.2 patterns and use try-with-resources for proper resource management
5. **VERIFY**: Run tests again to ensure they pass

### Step 4: Implement ListFilesTool
**Prompt:**
1. **FIRST**: Create test file `src/test/java/com/larseckart/core/tools/ListFilesToolTest.java` with tests for:
   - Directory listing with various path types
   - Hidden file filtering behavior
   - File/directory type identification
   - File size reporting
   - Alphabetical sorting
   - Directory not found scenarios
   - Permission denied scenarios
   - Parameter validation
   - Output format consistency
2. **SETUP**: Create test directories and files in `src/test/resources/` including hidden files
3. **RUN TESTS**: Execute `./gradlew test --tests "*ListFilesToolTest*"` and confirm they fail
4. **THEN**: Create `ListFilesTool` class in `core/tools/ListFilesTool.java` implementing exactly what tests expect:
   - Tool name should be "list_files"
   - Description should explain it lists directory contents
   - Parameter schema should require a "path" field (string, defaults to current directory) and optional "show_hidden" boolean field
   - Execute method should list files and directories in the specified path
   - Format output as a clear text listing showing file names, types (file/directory), and sizes
   - Handle both relative and absolute paths
   - Include error handling for directory not found, permission denied
   - Filter hidden files unless show_hidden is true
   - Sort output alphabetically for consistency
   - Return results in a format that's easy for Claude to parse and present to users
5. **VERIFY**: Run tests again to ensure they pass

### Step 5: Implement EditFileTool
**Prompt:**
1. **FIRST**: Create test file `src/test/java/com/larseckart/core/tools/EditFileToolTest.java` with tests for:
   - Successful text replacement scenarios
   - Backup file creation verification
   - Search text validation (exists/doesn't exist)
   - Multiple replacement occurrences
   - File access permission handling
   - Directory traversal attack prevention
   - Atomic operation behavior
   - Parameter validation
   - Success message format verification
2. **SETUP**: Create test files in `src/test/resources/` with various content for editing
3. **RUN TESTS**: Execute `./gradlew test --tests "*EditFileToolTest*"` and confirm they fail
4. **THEN**: Create `EditFileTool` class in `core/tools/EditFileTool.java` implementing exactly what tests expect:
   - Tool name should be "edit_file"
   - Description should explain it performs simple text replacement in files
   - Parameter schema should require "path" (string), "search_text" (string), and "replace_text" (string) fields
   - Execute method should find and replace text in the specified file
   - Create backup of original file before editing (with .backup extension)
   - Validate that the search text exists in the file before replacement
   - Return success message with details of changes made
   - Include comprehensive error handling for file access issues
   - Consider security implications - validate paths to prevent directory traversal
   - Use atomic operations where possible to prevent file corruption
   - Support both simple string replacement and consider regex patterns for future extension
5. **VERIFY**: Run tests again to ensure they pass

## Phase 3: Function Calling Integration

### Step 6: Update ConversationService
**Prompt:**
1. **FIRST**: Create test file `src/test/java/com/larseckart/core/services/ConversationServiceToolsTest.java` with tests for:
   - ToolRegistry dependency injection
   - Function calling integration with Claude API
   - Tool use detection in Claude responses
   - Tool execution via ToolRegistry
   - Tool result handling and response flow
   - Error handling for tool execution failures
   - Backward compatibility with existing conversation functionality
   - Multi-step conversation flow with tools
2. **SETUP**: Create mock tools and Claude API responses for testing
3. **RUN TESTS**: Execute `./gradlew test --tests "*ConversationServiceToolsTest*"` and confirm they fail
4. **THEN**: Modify the existing `ConversationService` class implementing exactly what tests expect:
   - Examine current `ConversationService.java` structure and API call methods
   - Add `ToolRegistry` as a dependency (inject via constructor)
   - Modify the Claude API call to include available tools in the function definitions
   - Parse Claude responses for tool use requests (look for function_calls in the response)
   - When tool use is detected, execute the requested tool via ToolRegistry
   - Return tool results back to Claude in the next message
   - Handle the conversation flow: user message → Claude response (possibly with tool use) → tool execution → tool result to Claude → final Claude response
   - Maintain existing conversation functionality for non-tool interactions
   - Add proper error handling for tool execution failures
   - Reference Anthropic Java SDK documentation for function calling patterns
5. **VERIFY**: Run tests again to ensure they pass

### Step 7: Enhance Message Handling
**Prompt:**
1. **FIRST**: Create test files for message handling enhancements:
   - `src/test/java/com/larseckart/core/domain/ConversationContextToolsTest.java` for context updates
   - `src/test/java/com/larseckart/adapters/cli/CLIToolsTest.java` for CLI adapter changes
   - `src/test/java/com/larseckart/adapters/web/WebToolsTest.java` for web adapter changes
   - Tests should cover: tool message types, conversation history, multi-step flows, serialization
2. **RUN TESTS**: Execute `./gradlew test --tests "*ToolsTest*"` and confirm they fail
3. **THEN**: Update message handling implementing exactly what tests expect:
   - Examine `ConversationContext.java` and message structures
   - Add support for new message types: tool_use and tool_result
   - Update the conversation context to store tool interactions in message history
   - Modify both CLI and web adapters to handle multi-step conversations with tool use
   - Ensure tool use messages are properly formatted for Claude API
   - Update the conversation flow to handle: user input → Claude response with tools → tool execution → continue conversation
   - Test that conversation context is maintained across tool interactions
   - Consider displaying tool usage to users (show what files are being read/modified)
   - Update serialization/deserialization if conversation context is persisted
4. **VERIFY**: Run tests again to ensure they pass

## Phase 4: Testing & Validation

### Step 8: Integration Testing
**Prompt:**
1. **FIRST**: Create comprehensive integration test files:
   - `src/test/java/com/larseckart/integration/ToolSystemIntegrationTest.java` for end-to-end flows
   - `src/test/java/com/larseckart/integration/CLIToolIntegrationTest.java` for CLI interface testing
   - `src/test/java/com/larseckart/integration/WebToolIntegrationTest.java` for web interface testing
   - `src/test/java/com/larseckart/integration/SecurityToolTest.java` for security validation
   - `src/test/java/com/larseckart/integration/PerformanceToolTest.java` for performance testing
   - Tests should cover: full conversation flows, backward compatibility, security boundaries, performance limits
2. **SETUP**: Create comprehensive test resources including files, directories, permissions scenarios
3. **RUN TESTS**: Execute `./gradlew test --tests "*Integration*"` and confirm they fail appropriately
4. **THEN**: Ensure all previously implemented components work together as expected by the integration tests:
   - Validate ReadFileTool, ListFilesTool, EditFileTool work in real scenarios
   - Create integration tests that test the full flow: user message → tool use → tool result → response
   - Test error handling scenarios and ensure meaningful error messages
   - Create test files and directories for tool testing (in test resources)
   - Validate that tool descriptions are clear and helpful for Claude
   - Test both CLI and web interfaces with tool functionality
   - Ensure backward compatibility - existing chat functionality should remain unchanged
   - Add performance tests for tool operations
   - Test security aspects - ensure tools can't access files outside allowed directories
5. **VERIFY**: Run all tests (`./gradlew test`) to ensure entire system works correctly

## Implementation Guidelines

### Security Considerations
- Implement path validation to prevent directory traversal attacks
- Consider implementing a whitelist of allowed directories for file operations
- Add file size limits to prevent resource exhaustion
- Validate file permissions before operations
- Consider implementing read-only mode for sensitive environments

### Error Handling Standards
- All tools should return human-readable error messages
- Include specific error codes for different failure types
- Log errors appropriately for debugging
- Ensure errors don't expose sensitive system information
- Provide helpful suggestions for common error scenarios

### Code Quality Requirements
- Follow existing code style and patterns from the codebase
- Add comprehensive JavaDoc comments
- Use proper exception handling with try-with-resources
- Follow dependency injection patterns for Spring Boot compatibility
- Write unit tests for all new components
- Ensure thread safety where applicable

### Testing Strategy
**MANDATORY TDD APPROACH:**
- **Unit tests FIRST** for individual tools and components - no implementation without failing tests
- **Integration tests FIRST** for end-to-end tool usage - test the expected behavior before building it
- **Mock Claude API responses** for testing conversation flows - write tests that verify expected interactions
- **File system test scenarios** - create comprehensive test resources before implementing file operations
- **Performance testing** for file operations - define performance requirements in tests first
- **Security testing** for path traversal and access control - write security tests before implementing access logic

**CRITICAL REMINDERS:**
- Every class must have a corresponding test file created BEFORE implementation
- Tests must fail initially to prove they're testing real functionality
- No feature is complete until all tests pass
- Refactoring is only done while tests remain green
- Integration tests validate that individual components work together correctly