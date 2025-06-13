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

# Search for lines indicating failed tests in the Gradle output
# Format: ClassName > testName FAILED
failures=$(awk '/^[^ ]+ > .+ FAILED$/ {print $0}' "$output_file")

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
    summary=$(grep -E '^[0-9]+ tests completed, [0-9]+ failed' "$output_file" | tail -1)
    if [ -n "$summary" ]; then
        printf "\n%s\n" "$summary"
    fi
else
    # If no failures, print success message
    echo "all tests passed"
fi
