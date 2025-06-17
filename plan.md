# Development Plan: Adding Tool System to Java AI Agent

## Debugging Guidelines
If me or you the LLM agent seem to go down too deep in a debugging/fixing rabbit hole in our conversations, remind me to take a breath and think about the bigger picture instead of hacking away. Say: "I think I'm stuck, let's TOUCH GRASS". IMPORTANT: Don't try to fix errors by yourself more than twice in a row. Then STOP. Don't do anything else.

## Current State ✅ COMPLETED
- ✅ Basic conversational AI with CLI/web interfaces
- ✅ Uses Anthropic Claude API for chat functionality
- ✅ **Tool system successfully implemented and integrated**
- ✅ **CLI application has working tool support with Claude API**
- ✅ **Comprehensive logging system for debugging and monitoring**

## Implementation Status

### Phase 1: Core Tool Architecture ✅ COMPLETED
1. ✅ **Create Tool Interface** (`core/domain/Tool.java`)
   - ✅ Tool contract with name, description, schema, and execute method
   - ✅ JSON schema support for parameter validation

2. ✅ **Build Tool Registry** (`core/services/ToolRegistry.java`)
   - ✅ Tool management and storage
   - ✅ Claude function definition conversion
   - ✅ Function call routing to appropriate tools

### Phase 2: File Manipulation Tools ✅ COMPLETED
3. ✅ **Implement ReadFileTool** (`core/tools/ReadFileTool.java`)
   - ✅ File reading with comprehensive error handling
   - ✅ Support for relative/absolute paths, multiple encodings
   - ✅ File size limits and security checks

4. ✅ **Implement ListFilesTool** (`core/tools/ListFilesTool.java`)
   - ✅ List directory contents with file/directory type indicators
   - ✅ Show file sizes with human-readable formatting
   - ✅ Filter hidden files (with optional show_hidden parameter)
   - ✅ Alphabetical sorting for consistent output
   - ✅ Comprehensive error handling for permissions and missing directories

5. ✅ **Implement EditFileTool** (`core/tools/EditFileTool.java`)
   - ✅ Simple text replacement editing with occurrence counting
   - ✅ Automatic backup file creation (.backup extension)
   - ✅ Search text validation before replacement
   - ✅ Comprehensive error handling and security checks
   - ✅ Directory traversal attack prevention
   - ✅ Integration with CLI and web applications

### Phase 3: Function Calling Integration ✅ COMPLETED
6. ✅ **Update ConversationService**
   - ✅ Function calling support integrated with Claude API
   - ✅ Tool use request parsing from Claude responses
   - ✅ Tool execution and result handling in conversation flow
   - ✅ Proper JSON schema format for Claude API compatibility
   - ✅ Tool parameter extraction using JsonValue visitor pattern

7. ✅ **Enhance Message Handling**
   - ✅ Tool execution integrated into conversation flow
   - ✅ CLI application wired with ToolRegistry and ReadFileTool
   - ✅ Conversation context maintained across tool interactions

### Phase 4: Testing & Validation ✅ COMPLETED
8. ✅ **Integration Testing**
   - ✅ Comprehensive test coverage for all implemented components
   - ✅ Tool system working with real Claude API
   - ✅ Error handling validated and functional
   - ✅ Tool descriptions optimized for Claude understanding

### Phase 5: Logging & Monitoring ✅ COMPLETED
9. ✅ **Comprehensive Logging System**
   - ✅ SLF4J + Logback configuration for both CLI and web modes
   - ✅ File-only logging to `logs/application.log` (clean CLI output)
   - ✅ Detailed debug information for service initialization, message processing, tool execution
   - ✅ Error tracking and API call monitoring
   - ✅ Increased token limit to 4K for larger responses

## Success Criteria Status
- ✅ Agent can read files via natural language commands (ReadFileTool working)
- ✅ Agent can list and edit files (ListFilesTool and EditFileTool fully implemented)
- ✅ Conversation maintains context across tool interactions
- ✅ Error handling provides meaningful feedback
- ✅ Tools integrate seamlessly with CLI interface
- ✅ Comprehensive logging for debugging and monitoring

## Next Steps (Future Development)
1. **Add more advanced tools** (search, execute commands, etc.)
2. **Enhance security** with additional path validation and access controls
3. **Optimize performance** for large file operations
4. **Implement regex support** in EditFileTool for advanced text replacement
5. **Add file creation and deletion tools**

## Technical Implementation Notes
- ✅ Hexagonal architecture patterns maintained
- ✅ Anthropic Java SDK function calling successfully integrated
- ✅ Backward compatibility preserved
- ✅ Security considerations implemented (file size limits, encoding validation)
- ✅ Comprehensive error handling with meaningful messages
- ✅ TDD approach followed with extensive test coverage