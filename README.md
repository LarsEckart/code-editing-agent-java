# Java AI Agent with Claude Integration

A Java-based conversational AI agent that communicates with Anthropic's Claude API, following Thorsten Ball's tutorial at [How to Build an Agent](https://ampcode.com/how-to-build-an-agent). 
This application demonstrates clean architecture principles and provides both CLI and web interfaces for interacting with Claude.

## AI Contributions

### Project Statistics

- **Total Commits**: 53
- **AI-Assisted Commits**: 35 (66.04%)
- **Total Lines Added**: 7206
- **AI-Assisted Lines Added**: 5549 (77.01%)
- **Total Lines Changed**: 10712
- **AI-Assisted Lines Changed**: 8338 (77.84%)

### Breakdown by AI Assistant

#### GitHub Copilot

- **Commits**: 1 (1.89%)
- **Lines Added**: 32
- **Lines Deleted**: 33
- **Lines Changed**: 65 (0.61%)

#### Claude

- **Commits**: 30 (56.60%)
- **Lines Added**: 4941
- **Lines Deleted**: 2307
- **Lines Changed**: 7248 (67.66%)

#### Amp

- **Commits**: 4 (7.55%)
- **Lines Added**: 576
- **Lines Deleted**: 449
- **Lines Changed**: 1025 (9.57%)


*Statistics are automatically updated on each commit.*

## Features

- **Dual Interface Support**: Run as a command-line application or web server
- **File Operations**: Built-in tools for reading, editing, and listing files
- **Clean Architecture**: Hexagonal architecture with clear separation of concerns
- **Hot Reloading**: Development server with automatic restart and live reload
- **Comprehensive Testing**: Full test suite with JUnit 5

## Prerequisites

- **Java 24** or higher
- **Gradle** (wrapper included)
- **Anthropic API Key** (required for Claude integration)

## Getting Started

### 1. Set Up API Key

You need an Anthropic API key to use this application. Get one from [Anthropic's Console](https://console.anthropic.com/).

Set the environment variable:

```bash
export code_editing_agent_api_key="your-api-key-here"
```

Or on Windows:
```cmd
set code_editing_agent_api_key=your-api-key-here
```

### 2. Clone and Build

```bash
git clone <repository-url>
cd code-editing-agent-java
./gradlew build
```

### 3. Install Git Hooks (Optional)

To automatically update AI contribution statistics in the README:

```bash
./scripts/install-hooks.sh
```

### 4. Run the Application

#### CLI Mode (Default)
```bash
./gradlew run
```

#### Web Mode
```bash
./gradlew run -Dapp.mode=web
```

Then open http://localhost:8080 in your browser.

> **Note**: The web UI implementation is currently incomplete. 
> While the Spring Boot dependencies and REST controller are in place, there are no Thymeleaf templates or proper web interface beyond a basic HTML file. 
> The full web functionality was previously built and then reverted. The web mode architecture is kept as an exercise for AI-assisted development and future implementation.

## Development

### Development Server (Recommended)

For active development with hot reloading:

```bash
# Start development server (web mode with auto-restart)
./dev-server.sh start

# Check status
./dev-server.sh status

# View logs
./dev-server.sh logs

# Stop server
./dev-server.sh stop
```

### Testing

```bash
# Run all tests with formatted output
./run_tests.sh

# Run specific test class
./gradlew test --tests "ConversationServiceTest"

# Build and run tests
./gradlew build
```

### Project Commands

- **Build**: `./gradlew build` (includes fatJar creation)
- **Test**: `./run_tests.sh` (runs tests with formatted output)
- **Clean**: `./gradlew clean`
- **Fat JAR**: Created automatically during build in `app/build/libs/`

## Architecture

This application follows hexagonal architecture principles:

### Core Components

- **ConversationService**: Handles Claude API communication
- **ChatService**: Orchestrates conversation flow
- **ConversationContext**: Domain model for conversation history
- **Tool System**: Extensible file operation tools (read, edit, list)

### Adapters

- **CLI Adapters**: Console-based I/O using Scanner and System.out
- **Web Adapters**: Spring Boot REST API

### Built-in Tools

The agent comes with file operation tools (CLI mode only):

- **ReadFileTool**: Read file contents
- **EditFileTool**: Modify existing files
- **ListFilesTool**: Browse directory contents

> **Security Note**: These file tools are designed exclusively for CLI mode where they operate on the user's local file system. In web mode, these tools would pose serious security risks by allowing web users to access the server's file system. A proper web implementation would require either disabling file tools entirely or implementing sandboxed alternatives.

## Configuration

### Environment Variables

- `code_editing_agent_api_key`: Required - Your Anthropic API key
- `app.mode`: Optional - Set to "web" for web mode (default: CLI)

### Model Configuration

The application uses Claude 3.5 Haiku by default. 
You can modify the model in `ConversationService.java:66`.

## Project Structure

```
app/src/main/java/com/larseckart/
├── App.java                    # Application entry point
├── adapters/
│   ├── cli/                   # Command-line interface
│   └── web/                   # Web interface (Spring Boot)
├── core/
│   ├── domain/                # Domain models
│   ├── ports/                 # Interface definitions
│   ├── services/              # Business logic
│   └── tools/                 # File operation tools
└── resources/
    └── static/index.html      # Web interface
```

## Usage Examples

### CLI Mode
```bash
$ ./gradlew run
> Hello! How can I help you today?
User: Can you read the contents of my config file?
Claude: I can help you read a file. What's the path to your config file?
User: ./app.properties
Claude: [Reads and displays file contents]
```

### Web Mode
1. Start: `./gradlew run -Dapp.mode=web`
2. Open: http://localhost:8080
3. Chat through the web interface (currently incomplete - see note above)

## Dependencies

- **Anthropic Java SDK** 2.0.0 - Claude API integration
- **Spring Boot** 3.5.0 - Web framework
- **JUnit 5** - Testing framework
- **Java 24** - Runtime platform

## Troubleshooting

### Common Issues

1. **Missing API Key**: Ensure `code_editing_agent_api_key` is set
2. **Java Version**: Requires Java 24 or higher
3. **Port Conflicts**: Web mode uses port 8080 by default
