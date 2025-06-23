#!/bin/bash

# Build script that applies formatting before building to avoid formatting failures

echo "Running build with automatic formatting..."

# Apply spotless formatting first
echo "Applying code formatting..."
./gradlew :app:spotlessApply

# Then run the build
echo "Building project..."
./gradlew :app:build

# Capture the exit code
BUILD_RESULT=$?

if [ $BUILD_RESULT -eq 0 ]; then
    echo "Build completed successfully!"
else
    echo "Build failed with exit code: $BUILD_RESULT"
fi

exit $BUILD_RESULT