# Development Plan: Adding Tool System to Java AI Agent

## Debugging Guidelines
If me or you the LLM agent seem to go down too deep in a debugging/fixing rabbit hole in our conversations, remind me to take a breath and think about the bigger picture instead of hacking away. Say: "I think I'm stuck, let's TOUCH GRASS". IMPORTANT: Don't try to fix errors by yourself more than twice in a row. Then STOP. Don't do anything else.

## Current State
- Basic conversational AI with CLI/web interfaces
- Uses Anthropic Claude API for chat functionality
- Missing core tool system from the reference tutorial

## Implementation Plan

### Phase 1: Core Tool Architecture
1. **Create Tool Interface** (`core/domain/Tool.java`)
   - Define tool contract with name, description, schema, and execute method
   - Support JSON schema for parameter validation

2. **Build Tool Registry** (`core/services/ToolRegistry.java`)
   - Manage available tools
   - Convert tools to Claude function definitions
   - Route function calls to appropriate tools

### Phase 2: File Manipulation Tools
3. **Implement ReadFileTool** (`core/tools/ReadFileTool.java`)
   - Read file contents with error handling
   - Support relative/absolute paths

4. **Implement ListFilesTool** (`core/tools/ListFilesTool.java`)
   - List directory contents
   - Filter and format output

5. **Implement EditFileTool** (`core/tools/EditFileTool.java`)
   - Simple text replacement editing
   - Backup and validation

### Phase 3: Function Calling Integration
6. **Update ConversationService**
   - Add function calling support to Claude API calls
   - Parse tool use requests from Claude responses
   - Execute tools and return results in conversation

7. **Enhance Message Handling**
   - Support tool use and tool result message types
   - Maintain conversation context with tool interactions

### Phase 4: Testing & Validation
8. **Integration Testing**
   - Test file operations through chat interface
   - Validate error handling and edge cases
   - Ensure tool descriptions are clear to Claude

## Success Criteria
- Agent can read, list, and edit files via natural language commands
- Conversation maintains context across tool interactions
- Error handling provides meaningful feedback
- Tools integrate seamlessly with existing CLI/web interfaces

## Technical Notes
- Follow existing hexagonal architecture patterns
- Use Anthropic Java SDK function calling features
- Maintain backward compatibility with current chat functionality
- Consider security implications of file system access