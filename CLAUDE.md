# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Application

This is a very simple AI agent, following this tutorial: https://ampcode.com/how-to-build-an-agent
It's implemented in java though.


## Build Commands

- **Build**: `./gradlew build` (includes fatJar creation)
- **Test**: `./gradlew test`
- **Single test**: `./gradlew test --tests "ClassName"`
- **Run CLI mode**: `./gradlew run` (default)
- **Run Web mode**: `./gradlew run -Dapp.mode=web`
- **Clean**: `./gradlew clean`

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
- **Spring Boot**: `3.4.6` (for web mode)
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