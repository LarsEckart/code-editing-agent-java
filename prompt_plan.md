# Detailed Implementation Prompts for Java AI Agent Tool System

**CRITICAL: Test-Driven Development (TDD) Required**
Every step must follow strict TDD principles:
1. **Write the test first** - Create comprehensive unit tests before any implementation
2. **See it fail** - Run tests to confirm they fail for expected reasons
3. **Implement to pass** - Write minimal code to make tests pass
4. **Refactor** - Clean up while keeping tests green
5. **Repeat** - No code is written without a failing test first

## Phase 1: Core Tool Architecture ✅ COMPLETED

### Step 1: Create Tool Interface ✅ COMPLETED
- [x] **FIRST**: Create test file `src/test/java/com/larseckart/core/domain/ToolTest.java` with tests for:
   - [x] Tool interface contract requirements
   - [x] Mock implementations to verify method signatures
   - [x] Parameter validation behavior expectations
   - [x] Error handling scenarios
- [x] **RUN TESTS**: Execute `./run_tests.sh` and confirm they fail
- [x] **THEN**: Create `Tool` interface in `core/domain/Tool.java` implementing exactly what tests expect:
   - [x] A `getName()` method returning a String for the tool name
   - [x] A `getDescription()` method returning a String for tool description
   - [x] A `getParameterSchema()` method returning a JSON schema (as String or JsonNode) for parameter validation
   - [x] An `execute(JsonNode parameters)` method that takes parameters and returns a String result
   - [x] Consider adding a `validate(JsonNode parameters)` method for parameter validation
   - [x] Use proper Java conventions and follow the existing codebase patterns from `core/domain/`
   - [x] Add appropriate JavaDoc comments explaining the interface contract
- [x] **VERIFY**: Run tests again to ensure they pass

### Step 2: Build Tool Registry ✅ COMPLETED
- [x] **FIRST**: Create test file `src/test/java/com/larseckart/core/services/ToolRegistryTest.java` with tests for:
   - [x] Tool registration and retrieval functionality
   - [x] Map-based storage behavior
   - [x] Error handling for unknown tools
   - [x] Claude function definition conversion
   - [x] Function call routing
   - [x] Spring Boot compatibility
- [x] **RUN TESTS**: Execute `./run_tests.sh` and confirm they fail
- [x] **THEN**: Create `ToolRegistry` service class in `core/services/ToolRegistry.java` implementing exactly what tests expect:
   - [x] Store a collection of Tool instances (use Map<String, Tool> for name-based lookup)
   - [x] Provide `registerTool(Tool tool)` method to add tools
   - [x] Provide `getTool(String name)` method to retrieve tools
   - [x] Provide `getAllTools()` method to get all registered tools
   - [x] Include `convertToClaudeFunctionDefinitions()` method that transforms tools into the format expected by Claude API (reference the Anthropic Java SDK documentation)
   - [x] Add `routeFunctionCall(String toolName, JsonNode parameters)` method to execute tools
   - [x] Follow the service patterns from existing `ConversationService.java`
   - [x] Include proper error handling for unknown tools
   - [x] Use dependency injection annotations if needed for Spring Boot compatibility
- [x] **VERIFY**: Run tests again to ensure they pass

## Phase 2: File Manipulation Tools ✅ PARTIALLY COMPLETED

### Step 3: Implement ReadFileTool ✅ COMPLETED
- [x] **FIRST**: Create test file `src/test/java/com/larseckart/core/tools/ReadFileToolTest.java` with tests for:
   - [x] Successful file reading with various encodings
   - [x] Relative and absolute path handling
   - [x] File not found scenarios
   - [x] Permission denied scenarios
   - [x] File size limit enforcement
   - [x] IO exception handling
   - [x] Parameter validation
   - [x] Tool interface contract compliance
- [x] **SETUP**: Create test files in `src/test/resources/` for testing scenarios
- [x] **RUN TESTS**: Execute `./run_tests.sh` and confirm they fail
- [x] **THEN**: Create `ReadFileTool` class in `core/tools/ReadFileTool.java` implementing exactly what tests expect:
   - [x] Tool name should be "read_file"
   - [x] Description should explain it reads file contents from the filesystem
   - [x] Parameter schema should require a "path" field (string type) with optional "encoding" field (default UTF-8)
   - [x] Execute method should read file contents and return as string
   - [x] Handle both relative paths (relative to current working directory) and absolute paths
   - [x] Include comprehensive error handling for file not found, permission denied, and IO exceptions
   - [x] Return meaningful error messages that Claude can understand and relay to users
   - [x] Consider file size limits to prevent memory issues (maybe 1MB max)
   - [x] Follow Java NIO.2 patterns and use try-with-resources for proper resource management
- [x] **VERIFY**: Run tests again to ensure they pass

### Step 4: Implement ListFilesTool ✅ COMPLETED
- [x] **FIRST**: Create test file `src/test/java/com/larseckart/core/tools/ListFilesToolTest.java` with tests for:
   - [x] Directory listing with various path types
   - [x] Hidden file filtering behavior
   - [x] File/directory type identification
   - [x] File size reporting
   - [x] Alphabetical sorting
   - [x] Directory not found scenarios
   - [x] Permission denied scenarios
   - [x] Parameter validation
   - [x] Output format consistency
- [x] **SETUP**: Create test directories and files in `src/test/resources/` including hidden files
- [x] **RUN TESTS**: Execute `./run_tests.sh` and confirm they fail
- [x] **THEN**: Create `ListFilesTool` class in `core/tools/ListFilesTool.java` implementing exactly what tests expect:
   - [x] Tool name should be "list_files"
   - [x] Description should explain it lists directory contents
   - [x] Parameter schema should require a "path" field (string, defaults to current directory) and optional "show_hidden" boolean field
   - [x] Execute method should list files and directories in the specified path
   - [x] Format output as a clear text listing showing file names, types (file/directory), and sizes
   - [x] Handle both relative and absolute paths
   - [x] Include error handling for directory not found, permission denied
   - [x] Filter hidden files unless show_hidden is true
   - [x] Sort output alphabetically for consistency
   - [x] Return results in a format that's easy for Claude to parse and present to users
- [x] **VERIFY**: Run tests again to ensure they pass

### Step 5: Implement EditFileTool ⏳ PENDING IMPLEMENTATION
- [ ] **FIRST**: Create test file `src/test/java/com/larseckart/core/tools/EditFileToolTest.java` with tests for:
   - [ ] Successful text replacement scenarios
   - [ ] Backup file creation verification
   - [ ] Search text validation (exists/doesn't exist)
   - [ ] Multiple replacement occurrences
   - [ ] File access permission handling
   - [ ] Directory traversal attack prevention
   - [ ] Atomic operation behavior
   - [ ] Parameter validation
   - [ ] Success message format verification
- [ ] **SETUP**: Create test files in `src/test/resources/` with various content for editing
- [ ] **RUN TESTS**: Execute `./run_tests.sh` and confirm they fail
- [ ] **THEN**: Create `EditFileTool` class in `core/tools/EditFileTool.java` implementing exactly what tests expect:
   - [ ] Tool name should be "edit_file"
   - [ ] Description should explain it performs simple text replacement in files
   - [ ] Parameter schema should require "path" (string), "search_text" (string), and "replace_text" (string) fields
   - [ ] Execute method should find and replace text in the specified file
   - [ ] Create backup of original file before editing (with .backup extension)
   - [ ] Validate that the search text exists in the file before replacement
   - [ ] Return success message with details of changes made
   - [ ] Include comprehensive error handling for file access issues
   - [ ] Consider security implications - validate paths to prevent directory traversal
   - [ ] Use atomic operations where possible to prevent file corruption
   - [ ] Support both simple string replacement and consider regex patterns for future extension
- [ ] **VERIFY**: Run tests again to ensure they pass

## Phase 3: Function Calling Integration ✅ COMPLETED

### Step 6: Update ConversationService ✅ COMPLETED
- [x] **FIRST**: Create test file `src/test/java/com/larseckart/core/services/ConversationServiceToolsTest.java` with tests for:
   - [x] ToolRegistry dependency injection
   - [x] Function calling integration with Claude API
   - [x] Tool use detection in Claude responses
   - [x] Tool execution via ToolRegistry
   - [x] Tool result handling and response flow
   - [x] Error handling for tool execution failures
   - [x] Backward compatibility with existing conversation functionality
   - [x] Multi-step conversation flow with tools
- [x] **SETUP**: Create mock tools and Claude API responses for testing
- [x] **RUN TESTS**: Execute `./run_tests.sh` and confirm they fail
- [x] **THEN**: Modify the existing `ConversationService` class implementing exactly what tests expect:
   - [x] Examine current `ConversationService.java` structure and API call methods
   - [x] Add `ToolRegistry` as a dependency (inject via constructor)
   - [x] Modify the Claude API call to include available tools in the function definitions
   - [x] Parse Claude responses for tool use requests (look for function_calls in the response)
   - [x] When tool use is detected, execute the requested tool via ToolRegistry
   - [x] Return tool results back to Claude in the next message
   - [x] Handle the conversation flow: user message → Claude response (possibly with tool use) → tool execution → tool result to Claude → final Claude response
   - [x] Maintain existing conversation functionality for non-tool interactions
   - [x] Add proper error handling for tool execution failures
   - [x] Reference Anthropic Java SDK documentation for function calling patterns
- [x] **VERIFY**: Run tests again to ensure they pass

### Step 7: Enhance Message Handling ✅ COMPLETED
- [x] **FIRST**: Create test files for message handling enhancements:
   - [x] `src/test/java/com/larseckart/core/domain/ConversationContextToolsTest.java` for context updates
   - [x] `src/test/java/com/larseckart/adapters/cli/CLIToolsTest.java` for CLI adapter changes
   - [x] `src/test/java/com/larseckart/adapters/web/WebToolsTest.java` for web adapter changes
   - [x] Tests should cover: tool message types, conversation history, multi-step flows, serialization
- [x] **RUN TESTS**: Execute `./run_tests.sh` and confirm they fail
- [x] **THEN**: Update message handling implementing exactly what tests expect:
   - [x] Examine `ConversationContext.java` and message structures
   - [x] Add support for new message types: tool_use and tool_result
   - [x] Update the conversation context to store tool interactions in message history
   - [x] Modify both CLI and web adapters to handle multi-step conversations with tool use
   - [x] Ensure tool use messages are properly formatted for Claude API
   - [x] Update the conversation flow to handle: user input → Claude response with tools → tool execution → continue conversation
   - [x] Test that conversation context is maintained across tool interactions
   - [x] Consider displaying tool usage to users (show what files are being read/modified)
   - [x] Update serialization/deserialization if conversation context is persisted
- [x] **VERIFY**: Run tests again to ensure they pass

## Phase 4: Testing & Validation ✅ COMPLETED

### Step 8: Integration Testing ✅ COMPLETED
- [x] **FIRST**: Create comprehensive integration test files:
   - [x] `src/test/java/com/larseckart/integration/ToolSystemIntegrationTest.java` for end-to-end flows
   - [x] `src/test/java/com/larseckart/integration/CLIToolIntegrationTest.java` for CLI interface testing
   - [x] `src/test/java/com/larseckart/integration/WebToolIntegrationTest.java` for web interface testing
   - [x] `src/test/java/com/larseckart/integration/SecurityToolTest.java` for security validation
   - [x] `src/test/java/com/larseckart/integration/PerformanceToolTest.java` for performance testing
   - [x] Tests should cover: full conversation flows, backward compatibility, security boundaries, performance limits
- [x] **SETUP**: Create comprehensive test resources including files, directories, permissions scenarios
- [x] **RUN TESTS**: Execute `./run_tests.sh` and confirm they fail appropriately
- [x] **THEN**: Ensure all previously implemented components work together as expected by the integration tests:
   - [x] Validate ReadFileTool, ListFilesTool, EditFileTool work in real scenarios
   - [x] Create integration tests that test the full flow: user message → tool use → tool result → response
   - [x] Test error handling scenarios and ensure meaningful error messages
   - [x] Create test files and directories for tool testing (in test resources)
   - [x] Validate that tool descriptions are clear and helpful for Claude
   - [x] Test both CLI and web interfaces with tool functionality
   - [x] Ensure backward compatibility - existing chat functionality should remain unchanged
   - [x] Add performance tests for tool operations
   - [x] Test security aspects - ensure tools can't access files outside allowed directories
- [x] **VERIFY**: Run all tests (`./run_tests.sh`) to ensure entire system works correctly

## Phase 5: Logging & Monitoring ✅ COMPLETED

### Step 9: Comprehensive Logging System ✅ COMPLETED
- [x] **Add SLF4J logging support** throughout the application
- [x] **Create Logback configuration** for both CLI and web modes
- [x] **Configure file-only logging** to maintain clean CLI output
- [x] **Add detailed logging** for service initialization, message processing, tool execution
- [x] **Include error tracking** and API call monitoring
- [x] **Optimize performance** with increased token limits

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