# Gemini ReadFile and EditFile Tools Implementation Plan - COMPLETED

## Overview
Extended the existing GeminiTools class to include `readFile` and `editFile` methods, following the same patterns as the existing `listFiles` implementation and maintaining feature parity with the Anthropic equivalents.

## Implementation Completed ✅

### 1. ReadFile Method Added to GeminiTools ✅
- **Method signature**: `public static String readFile(String path, String encoding)`
- **Features implemented**:
  - Support for both absolute and relative paths
  - Optional encoding parameter (defaults to UTF-8)
  - 1MB file size limit for safety
  - Comprehensive error handling
  - Path resolution using helper method `resolveFilePath()`

### 2. EditFile Method Added to GeminiTools ✅
- **Method signature**: `public static String editFile(String path, String searchText, String replaceText)`
- **Features implemented**:
  - Simple text replacement functionality
  - Automatic backup creation (.backup extension)
  - Validation that search text exists before replacement
  - Security checks to prevent dangerous path operations
  - Occurrence counting and reporting

### 3. GeminiProvider Registration Updated ✅
- Extended the tool registration in `GeminiProvider.sendMessage()` to include both new methods
- Uses reflection to register `readFile` and `editFile` alongside existing `listFiles`
- Handles registration errors gracefully with proper logging

### 4. Implementation Details

#### ReadFile Method Implementation:
- Handles null parameters with defaults (path=".", encoding="UTF-8")
- Resolves relative paths against current working directory
- Checks file existence, size limits, and permissions
- Reads file content with specified encoding
- Returns formatted error messages for all failure cases

#### EditFile Method Implementation:
- Validates all required parameters are present
- Applies security restrictions (prevents ../traversal, system paths)
- Verifies file exists and is regular file
- Checks search text exists before attempting replacement
- Creates backup before modification
- Performs replacement and writes back to file
- Returns success message with occurrence count

### 5. Error Handling Strategy ✅
- Returns descriptive error messages as strings (consistent with existing pattern)
- Uses same error message formats as Anthropic tools for consistency
- Includes proper logging for debugging
- Handles all common file system exceptions

### 6. File Structure Changes ✅
```
app/src/main/java/com/larseckart/tools/gemini/GeminiTools.java (modified - added readFile and editFile methods)
app/src/main/java/com/larseckart/adapters/ai/GeminiProvider.java (modified - updated tool registration)
```

### 7. Code Changes Summary
- **GeminiTools.java**: Added imports for `Charset`, `NoSuchFileException`, and `StandardCopyOption`
- **GeminiTools.java**: Added constants `MAX_FILE_SIZE` and `DEFAULT_ENCODING`
- **GeminiTools.java**: Added `readFile(String path, String encoding)` method
- **GeminiTools.java**: Added `editFile(String path, String searchText, String replaceText)` method  
- **GeminiTools.java**: Added private helper method `resolveFilePath(String pathStr)`
- **GeminiProvider.java**: Updated tool registration to include readFile and editFile methods

## Benefits Achieved ✅
- Maintains consistency with existing Anthropic tool functionality
- Uses proven patterns from existing GeminiTools implementation
- Provides full file manipulation capabilities for Gemini users
- Follows established security and error handling practices
- Ready for testing and production use

## Next Steps
- Test the implementation with various file operations
- Verify tool registration works correctly in GeminiProvider
- Document usage examples for end users