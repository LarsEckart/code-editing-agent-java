# Detailed Implementation Prompts for Java AI Agent Tool System

## Phase 1: Core Tool Architecture

### Step 1: Create Tool Interface
**Prompt:** Create a `Tool` interface in `core/domain/Tool.java` that defines the contract for all tools in the system. The interface should include:
- A `getName()` method returning a String for the tool name
- A `getDescription()` method returning a String for tool description
- A `getParameterSchema()` method returning a JSON schema (as String or JsonNode) for parameter validation
- An `execute(JsonNode parameters)` method that takes parameters and returns a String result
- Consider adding a `validate(JsonNode parameters)` method for parameter validation
- Use proper Java conventions and follow the existing codebase patterns from `core/domain/`
- Add appropriate JavaDoc comments explaining the interface contract

### Step 2: Build Tool Registry
**Prompt:** Create a `ToolRegistry` service class in `core/services/ToolRegistry.java` that manages available tools. Implementation should:
- Store a collection of Tool instances (use Map<String, Tool> for name-based lookup)
- Provide `registerTool(Tool tool)` method to add tools
- Provide `getTool(String name)` method to retrieve tools
- Provide `getAllTools()` method to get all registered tools
- Include `convertToClaudeFunctionDefinitions()` method that transforms tools into the format expected by Claude API (reference the Anthropic Java SDK documentation)
- Add `routeFunctionCall(String toolName, JsonNode parameters)` method to execute tools
- Follow the service patterns from existing `ConversationService.java`
- Include proper error handling for unknown tools
- Use dependency injection annotations if needed for Spring Boot compatibility

## Phase 2: File Manipulation Tools

### Step 3: Implement ReadFileTool
**Prompt:** Create `ReadFileTool` class in `core/tools/ReadFileTool.java` implementing the `Tool` interface. Requirements:
- Tool name should be "read_file"
- Description should explain it reads file contents from the filesystem
- Parameter schema should require a "path" field (string type) with optional "encoding" field (default UTF-8)
- Execute method should read file contents and return as string
- Handle both relative paths (relative to current working directory) and absolute paths
- Include comprehensive error handling for file not found, permission denied, and IO exceptions
- Return meaningful error messages that Claude can understand and relay to users
- Consider file size limits to prevent memory issues (maybe 1MB max)
- Follow Java NIO.2 patterns and use try-with-resources for proper resource management

### Step 4: Implement ListFilesTool
**Prompt:** Create `ListFilesTool` class in `core/tools/ListFilesTool.java` implementing the `Tool` interface. Requirements:
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

### Step 5: Implement EditFileTool
**Prompt:** Create `EditFileTool` class in `core/tools/EditFileTool.java` implementing the `Tool` interface. Requirements:
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

## Phase 3: Function Calling Integration

### Step 6: Update ConversationService
**Prompt:** Modify the existing `ConversationService` class to add function calling support. Changes needed:
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

### Step 7: Enhance Message Handling
**Prompt:** Update message handling throughout the application to support tool interactions. Changes needed:
- Examine `ConversationContext.java` and message structures
- Add support for new message types: tool_use and tool_result
- Update the conversation context to store tool interactions in message history
- Modify both CLI and web adapters to handle multi-step conversations with tool use
- Ensure tool use messages are properly formatted for Claude API
- Update the conversation flow to handle: user input → Claude response with tools → tool execution → continue conversation
- Test that conversation context is maintained across tool interactions
- Consider displaying tool usage to users (show what files are being read/modified)
- Update serialization/deserialization if conversation context is persisted

## Phase 4: Testing & Validation

### Step 8: Integration Testing
**Prompt:** Create comprehensive tests and validation for the tool system. Implementation should:
- Create test files in `src/test/java/com/larseckart/core/tools/` for each tool
- Test ReadFileTool with various scenarios: valid files, missing files, permission issues, large files
- Test ListFilesTool with different directories, hidden files, empty directories
- Test EditFileTool with various replacement scenarios, backup creation, error conditions
- Create integration tests that test the full flow: user message → tool use → tool result → response
- Test error handling scenarios and ensure meaningful error messages
- Create test files and directories for tool testing (in test resources)
- Validate that tool descriptions are clear and helpful for Claude
- Test both CLI and web interfaces with tool functionality
- Ensure backward compatibility - existing chat functionality should remain unchanged
- Add performance tests for tool operations
- Test security aspects - ensure tools can't access files outside allowed directories

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
- Unit tests for individual tools and components
- Integration tests for end-to-end tool usage
- Mock Claude API responses for testing conversation flows
- Test with various file system scenarios
- Performance testing for file operations
- Security testing for path traversal and access control