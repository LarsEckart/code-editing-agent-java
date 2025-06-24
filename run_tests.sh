#!/bin/bash

# Exit immediately if a command exits with a non-zero status,
# treat unset variables as an error, and fail if any command in a pipeline fails
set -euo pipefail

# Create a temporary file to store the test output
output_file=$(mktemp)
# Ensure the temporary file is deleted on script exit or interruption
trap 'rm -f "$output_file"' EXIT INT TERM

# Run Gradle spotlessApply and tests with info-level output, redirect all output (stdout and stderr) to the temp file
# The '|| true' ensures this script continues even if tests fail
# --no-build-cache forces tests to run even if results are cached, but allows incremental compilation
./gradlew spotlessApply test --no-build-cache -i >"$output_file" 2>&1 || true

# Check for compilation errors first
compilation_errors=$(grep -E "error:|[0-9]+ errors?$|Compilation failed" "$output_file" || true)

if [ -n "$compilation_errors" ]; then
    # If there are compilation errors, print them
    printf "\nCompilation errors found:\n"
    
    # Extract compilation errors with full file path and line number
    awk '
    # Match Java compilation error pattern: /path/to/file.java:line: error: message
    /^\/.*\.java:[0-9]+: error:/ {
        # Use regex substitution to extract parts
        # First extract the error message (everything after ": error: ")
        error_msg = $0;
        sub(/^.*: error: /, "", error_msg);
        
        # Extract file path and line number (everything before ": error: ")
        file_and_line = $0;
        sub(/: error:.*$/, "", file_and_line);
        
        # Split by colons to get file path and line number
        split(file_and_line, parts, ":");
        # Last part is line number, everything else is file path
        line_number = parts[length(parts)];
        file_path = file_and_line;
        sub(/:[0-9]+$/, "", file_path);
        
        # Get just the filename from the full path
        split(file_path, path_parts, "/");
        filename = path_parts[length(path_parts)];
        
        printf "  ⎿  %s:%s - %s\n", filename, line_number, error_msg;
        
        # Print the next line if it contains the problematic code
        getline;
        if ($0 && !/^$/ && !/^\s*\^/) {
            gsub(/^\s+/, "      ", $0);  # Indent the code line
            print $0;
        }
        
        # Print the caret line if it exists
        getline;
        if ($0 && /^\s*\^/) {
            gsub(/^\s+/, "      ", $0);  # Indent the caret line
            print $0;
        }
    }
    
    # Also capture error count
    /[0-9]+ errors?$/ {
        printf "  ⎿  Total: %s\n", $0;
    }
    ' "$output_file"
    
    # Also show the "What went wrong" section for additional context
    printf "\nDetailed error information:\n"
    awk '
    /^\* What went wrong:/ { in_section = 1; print "  " $0; next; }
    /^\* Try:/ { in_section = 0; }
    in_section && NF > 0 { print "  " $0; }
    ' "$output_file"
    
    exit 1
fi

# Search for lines indicating failed tests in the Gradle output
# Format: ClassName > testName FAILED
failures=$(awk '/^[^ ]+ > .+ FAILED$/ {print $0}' "$output_file" || true)

if [ -n "$failures" ]; then
    # If there are failures, print them and their stacktraces
    printf "\nFailed tests and stacktraces:\n"
    awk '
    BEGIN {in_fail=0;}
    # Detect the start of a failed test entry
    /^[^ ]+ > .+ FAILED$/ {
        in_fail=1;
        print "\n" $0;
        next;
    }
    # While in a failed test entry, print stacktrace lines and related info
    in_fail==1 {
        if (/^$/) {
            # End of stacktrace on empty line
            in_fail=0;
        } else {
            print $0;
        }
    }
    ' "$output_file"
    # Capture and print the summary line at the end
    summary=$(grep -E '^[0-9]+ tests completed, [0-9]+ failed' "$output_file" | tail -1 || true)
    if [ -n "$summary" ]; then
        printf "\n%s\n" "$summary"
    fi
    exit 1
else
    # Check if build was successful
    if grep -q "BUILD SUCCESSFUL" "$output_file"; then
        echo "all tests passed"
    else
        echo "Build failed but no test failures detected"
        exit 1
    fi
fi
