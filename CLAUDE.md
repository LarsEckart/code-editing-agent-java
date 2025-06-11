# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Application

This is a very simple AI agent, following this tutorial: https://ampcode.com/how-to-build-an-agent
It's implemented in java though.


## Build Commands

- **Build**: `./gradlew build` (includes fatJar creation)
- **Test**: `./gradlew test`
- **Single test**: `./gradlew test --tests "ClassName"`
- **Run**: `./gradlew run`
- **Clean**: `./gradlew clean`

## Architecture Overview

This is a Java-based conversational AI agent that communicates with the Anthropic Claude API and supports tool execution. The application follows a layered architecture:

### Core Components

- **Agent** (`Agent.java`): Main conversation loop that handles user input, sends messages to Claude, and executes tool calls
- **Client** (`Client.java`): HTTP client for Anthropic API communication, handles request/response serialization and tool call parsing
- **Context** (`Context.java`): Simple conversation history storage
- **App** (`App.java`): Entry point that wires up components and starts the agent

### Tool System

- **ToolDefinition** (`tool/ToolDefinition.java`): Interface for defining tools with JSON schema and execution logic
- **Built-in Tools** (`tools/`): File system tools (ReadFileTool, EditFileTool, ListFilesTool) that allow Claude to interact with local files

### Key Dependencies

- **Anthropic Java SDK**: `com.anthropic:anthropic-java:1.4.0`
- **Jackson**: For JSON processing
- **Java 21**: Required runtime version
- **JUnit 5**: For testing

### Configuration

- Set `ANTHROPIC_API_KEY` environment variable for API access
- Main class: `com.larseckart.App`
- Uses Claude 3.5 Haiku model by default (configurable in `Client.java:66`)

### Code Style

- Root package: `com.larseckart`
- 2-space indentation
- PascalCase for classes, camelCase for methods/variables
- Dependencies managed via `gradle/libs.versions.toml`