# Gemini List Files Tool Implementation Plan

## Overview
Implement the "list files" tool for Google Gemini using their native function calling mechanism, maintaining feature parity with the existing Anthropic implementation.

## Architecture Decision
- **Approach**: Create a Gemini-specific tool registration mechanism
- **Execution**: Let Gemini automatically execute functions during conversation
- **Package Structure**: `com.larseckart.tools.gemini` for Gemini-specific tools

## Implementation Details

### 1. Package Structure
- Create new package: `com.larseckart.tools.gemini`
- Create class: `GeminiTools` containing static methods for all Gemini tools

### 2. List Files Method Signature
```java
package com.larseckart.tools.gemini;

public class GeminiTools {
    /**
     * Lists the contents of a directory, including files and subdirectories
     * 
     * @param path The directory path to list. Defaults to current directory if not provided
     * @param showHidden Whether to show hidden files (files starting with dot). Defaults to false
     * @return Directory listing or error message
     */
    public static String listFiles(String path, Boolean showHidden) {
        // Implementation details below
    }
}
```

### 3. Method Implementation Details
The method will:
- Accept nullable parameters (path defaults to ".", showHidden defaults to false)
- Normalize the path using `Paths.get(pathStr).normalize()`
- Check if path exists and is a directory
- List files with filtering based on showHidden parameter
- Format output similar to existing ListFilesTool:
  - Show absolute path of directory
  - List each file with [file] or [directory] indicator
  - Include file sizes for regular files
  - Sort alphabetically (case-insensitive)
- Return error messages as strings for consistency

### 4. GeminiProvider Updates
Update the `GeminiProvider.sendMessage()` method to:
1. Import the GeminiTools class
2. Register the listFiles method using reflection:
   ```java
   Method listFilesMethod = GeminiTools.class.getDeclaredMethod("listFiles", String.class, Boolean.class);
   ```
3. Add the method to the tool configuration:
   ```java
   GenerateContentConfig config = GenerateContentConfig.builder()
       .tools(Tool.builder().functions(listFilesMethod))
       .build();
   ```
4. Update conversation handling to work with function calls

### 5. Error Handling
- File not found: Return "Error: Directory not found: [path]"
- Not a directory: Return "Error: Path is not a directory: [path]"
- Permission denied: Return "Error: Permission denied - [message]"
- Other IO errors: Return "Error: [message]"

### 6. File Size Formatting
Use the same formatting as ListFilesTool:
- < 1KB: "[n] bytes"
- < 1MB: "[n.n] KB"
- < 1GB: "[n.n] MB"
- >= 1GB: "[n.n] GB"

### 7. Testing Strategy
- Create manual test to verify tool registration
- Test with various inputs:
  - Default parameters (null values)
  - Valid directory paths
  - Invalid paths
  - Hidden files toggle
  - Permission errors
- Verify output format matches existing tool

## File Structure
```
app/src/main/java/com/larseckart/
├── tools/
│   └── gemini/
│       └── GeminiTools.java (new)
└── adapters/
    └── ai/
        └── GeminiProvider.java (modified)
```

## Implementation Order
1. Create the `com.larseckart.tools.gemini` package
2. Implement `GeminiTools.listFiles()` method
3. Update `GeminiProvider` to register and use the tool
4. Test the implementation

## Future Considerations
- This pattern can be extended for other tools (ReadFile, EditFile, etc.)
- Consider creating a base class or utility methods for common functionality
- May want to add logging similar to existing tools