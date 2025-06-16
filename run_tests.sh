#!/bin/bash

# Exit immediately if a command exits with a non-zero status,
# treat unset variables as an error, and fail if any command in a pipeline fails
set -euo pipefail

# Create a temporary file to store the test output
output_file=$(mktemp)
# Ensure the temporary file is deleted on script exit or interruption
trap 'rm -f "$output_file"' EXIT INT TERM

# Run Gradle tests with info-level output, redirect all output (stdout and stderr) to the temp file
# The '|| true' ensures this script continues even if tests fail
./gradlew test -i >"$output_file" 2>&1 || true

# Check for compilation errors first
compilation_errors=$(grep -E "error: cannot find symbol|[0-9]+ errors?$|Compilation failed" "$output_file" || true)

if [ -n "$compilation_errors" ]; then
    # If there are compilation errors, print them
    printf "\nCompilation errors found:\n"
    # Extract and format compilation errors from the "What went wrong" section
    awk '
    /^\* What went wrong:/ {in_error_section=1; next;}
    /^\* Try:/ {in_error_section=0;}
    in_error_section && /error: cannot find symbol/ {
        # Print the file and error
        print "  ⎿  Error: " $0;
        # Read next lines for context
        getline; if ($0 && !/^$/ && !/^\s*\^/) print "       " $0;
        getline; if ($0 && !/^$/ && !/^\s*\^/) print "       " $0;
    }
    END {
        # Print error count at the end
        if (error_count > 0) {
            print "  ⎿  " error_count " errors found";
        }
    }
    /[0-9]+ errors?$/ && !printed_count {
        error_count = $1;
        printed_count = 1;
    }
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
