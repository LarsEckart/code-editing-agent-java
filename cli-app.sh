#!/bin/bash

# CLI application launcher
# Usage: ./cli-app.sh

./gradlew :app:processResources :app:classes :app:fatJar -q > /dev/null 2>&1

if [ $? -ne 0 ]; then
  echo "Build failed. Running build with output to show errors:"
  ./gradlew build
  exit 1
fi

echo ""

# Start the CLI application using the fat JAR (clean interface)
java -Dapp.mode=cli -jar app/build/libs/app-all.jar
