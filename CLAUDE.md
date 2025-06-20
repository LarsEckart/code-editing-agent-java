# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Application

This is a very simple AI agent, following this tutorial: https://ampcode.com/how-to-build-an-agent
It's implemented in java though.


## Project Structure

This is a multi-module Gradle project with Kotlin DSL:
- **Root directory**: Contains `settings.gradle.kts` and project scripts
- **Main module**: `app/` directory contains `build.gradle.kts` and all source code
- **Build file**: `app/build.gradle.kts` (NOT `build.gradle` at root level)

## Build Commands

### CLI Mode
- **Start CLI**: `./cli-app.sh` (builds and runs interactively)
- **View CLI logs**: `tail -f logs/application-cli.log`

### Web Mode  
- **Start dev server**: `./dev-server.sh start` (with hot reloading)
- **Stop dev server**: `./dev-server.sh stop`
- **View web logs**: `./dev-server.sh logs`

### Build & Test
- **Build**: `./gradlew build` (includes fatJar creation)
- **Test**: `./run_tests.sh` (runs tests with formatted output)
- **Single test**: `./gradlew test --tests "ClassName"`
- **Clean**: `./gradlew clean`

## Logging Configuration

### Separate Log Files
- **CLI Mode**: `logs/application-cli.log`
- **Web Mode**: `logs/application-web.log`
- **Log Rotation**: Daily rotation with 30-day retention and 100MB total size cap
- **Clean Output**: Logback status messages suppressed in CLI mode for clean user experience

### Configuration Details
- **Logback config**: `app/src/main/resources/logback.xml`
- **Mode detection**: Uses `app.mode` system property (set automatically by application entry points)
- **Pattern**: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`
- **Log levels**: INFO for application, DEBUG for ConversationService, WARN for Spring/external libs

## Development Server

For web development with hot reloading:

- **Start**: `./dev-server.sh start` (starts web mode with hot reloading)
- **Stop**: `./dev-server.sh stop`
- **Restart**: `./dev-server.sh restart`
- **Status**: `./dev-server.sh status`
- **Logs**: `./dev-server.sh logs`

The development server includes:
- **Automatic restart** when Java classes change
- **Live reload** for static resources and templates (after build)
- **Development configuration** with disabled caching
- **Background process management** with PID tracking
- **Logs to**: `logs/application-web.log`

**Note**: Hot reloading works for Java code changes. Static files (HTML, CSS, JS) in `src/main/resources/static/` require being "built" (copied to classpath) to trigger live reload - when running from command line with Gradle, this typically requires a server restart to see changes.

## Architecture Overview

This is a Java-based conversational AI agent that communicates with the Anthropic Claude API. The application follows a hexagonal architecture with clear separation between business logic and I/O adapters.

### Core Components

- **ConversationService** (`core/services/ConversationService.java`): Business logic for API communication
- **ChatService** (`core/services/ChatService.java`): Orchestrates conversation flow using ports
- **ConversationContext** (`core/domain/ConversationContext.java`): Domain model for conversation history
- **InputPort/OutputPort** (`core/ports/`): Interfaces for I/O abstraction

### Adapters

- **CLI Adapters** (`adapters/cli/`): Console-based implementations using Scanner and System.out
- **Web Adapters** (`adapters/web/`): Spring Boot REST API and HTML frontend
- **App** (`App.java`): Entry point that selects CLI or web mode based on system property

### Running Modes

The application supports two modes:
- **CLI Mode** (default): Interactive command-line interface
- **Web Mode**: HTTP server with REST API and web interface at http://localhost:8080

### Key Dependencies

- **Anthropic Java SDK**: `com.anthropic:anthropic-java:2.0.0`
- **Spring Boot**: `3.5.0` (for web mode)
- **Java 24**: Required runtime version
- **JUnit 5**: For testing

### Configuration

- Set `code_editing_agent_api_key` environment variable for API access
- Main class: `com.larseckart.App`
- Uses Claude 3.5 Haiku model by default (configurable in `ConversationService.java`)
- **CLI Mode**: Set by `CliApplication.main()` with `System.setProperty("app.mode", "cli")`
- **Web Mode**: Set by `WebApplication.main()` with `System.setProperty("app.mode", "web")`

### Code Style

- Root package: `com.larseckart`
- 2-space indentation
- PascalCase for classes, camelCase for methods/variables
- Dependencies managed via `gradle/libs.versions.toml`

### Test Conventions

- All test classes use `@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)`
- Test method names use snake_case format (e.g., `should_handle_empty_file()`)
- Display names are automatically generated from method names ("should handle empty file")
- Use `@DisabledOnOs(OS.WINDOWS)` instead of manual OS detection for platform-specific tests
- Avoid manual `@DisplayName` annotations