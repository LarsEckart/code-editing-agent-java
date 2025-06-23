# Java AI Agent with Multi-Provider Support

A Java-based conversational AI agent that supports multiple AI providers (Anthropic Claude and Google Gemini), following Thorsten Ball's tutorial at [How to Build an Agent](https://ampcode.com/how-to-build-an-agent). 
This application demonstrates clean architecture principles and provides both CLI and web interfaces for interacting with AI models.

## AI Contributions

### Project Statistics

- **Total Commits**: 60
- **AI-Assisted Commits**: 41 (68.33%)
- **Total Lines Added**: 8622
- **AI-Assisted Lines Added**: 6952 (80.63%)
- **Total Lines Changed**: 12883
- **AI-Assisted Lines Changed**: 10480 (81.35%)

### Breakdown by AI Assistant

#### Claude Code

- **Commits**: 32 (53.33%)
- **Lines Added**: 5623
- **Lines Deleted**: 2645
- **Lines Changed**: 8268 (64.18%)

#### Amp

- **Commits**: 7 (11.67%)
- **Lines Added**: 1258
- **Lines Deleted**: 809
- **Lines Changed**: 2067 (16.04%)

#### GitHub Copilot

- **Commits**: 2 (3.33%)
- **Lines Added**: 71
- **Lines Deleted**: 74
- **Lines Changed**: 145 (1.13%)


*Statistics are automatically updated on each commit.*

## Features

- **Multi-Provider Support**: Choose between Anthropic Claude and Google Gemini
- **Dual Interface Support**: Run as a command-line application or web server
- **File Operations**: Built-in tools for reading, editing, and listing files (supports both Claude and Gemini)
- **Clean Architecture**: Hexagonal architecture with clear separation of concerns
- **Hot Reloading**: Development server with automatic restart and live reload
- **Separate Logging**: Mode-specific log files (CLI: `logs/application-cli.log`, Web: `logs/application-web.log`)
- **Simple Launchers**: Clean scripts for both CLI (`./cli-app.sh`) and web (`./dev-server.sh`) modes
- **Comprehensive Testing**: Full test suite with JUnit 5

## Prerequisites

- **Java 24** or higher
- **Gradle** (wrapper included)
- **API Key**: Either Anthropic API Key (for Claude) or Google API Key (for Gemini)

## Getting Started

### 1. Set Up API Key

#### For Anthropic Claude (default)
Get an API key from [Anthropic's Console](https://console.anthropic.com/).

```bash
export code_editing_agent_api_key="your-api-key-here"
```

#### For Google Gemini
Get an API key from [Google AI Studio](https://aistudio.google.com/).

```bash
export GOOGLE_API_KEY="your-api-key-here"
export AI_PROVIDER=gemini
```

### 2. Clone and Build

```bash
git clone <repository-url>
cd code-editing-agent-java
./run_build.sh
```

### 3. Install Git Hooks (Optional)

To automatically update AI contribution statistics in the README:

```bash
./scripts/install-hooks.sh
```

### 4. Run the Application

#### CLI Mode (Default)
```bash
./cli-app.sh
```

#### Web Mode
```bash
./dev-server.sh start
```

Then open http://localhost:8080 in your browser to access the chat interface.

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
./run_build.sh
```

### Project Commands

#### CLI Mode
- **Start CLI**: `./cli-app.sh` (builds and runs interactively)
- **View CLI logs**: `tail -f logs/application-cli.log`

#### Web Mode
- **Start dev server**: `./dev-server.sh start` (with hot reloading)
- **Stop dev server**: `./dev-server.sh stop`
- **View web logs**: `./dev-server.sh logs`

#### Build & Test
- **Build**: `./run_build.sh` (applies formatting and builds - recommended)
- **Alternative build**: `./gradlew build` (includes fatJar creation)
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
- **Web Adapters**: Spring Boot REST API with HTML chat interface

### Built-in Tools

The agent comes with file operation tools:

- **ReadFileTool**: Read file contents
- **EditFileTool**: Modify existing files  
- **ListFilesTool**: Browse directory contents

> **Note**: Tool support is available for both Anthropic Claude and Google Gemini providers.

> **Security Note**: These file tools are designed exclusively for CLI mode where they operate on the user's local file system. In web mode, these tools would pose serious security risks by allowing web users to access the server's file system. A proper web implementation would require either disabling file tools entirely or implementing sandboxed alternatives.

## Configuration

### Environment Variables

- `code_editing_agent_api_key`: Required for Claude - Your Anthropic API key
- `GOOGLE_API_KEY`: Required for Gemini - Your Google API key
- `AI_PROVIDER`: Optional - Set to "gemini" to use Gemini (default: Claude)
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
$ ./cli-app.sh
Building application...
Starting CLI application...

Chat with a LLM (use 'ctrl-c' to quit or press Enter on empty line)
You: Can you read the contents of my config file?
LarsGPT: I can help you read a file. What's the path to your config file?
You: ./app.properties
LarsGPT: [Reads and displays file contents]
```

### Web Mode
1. Start: `./dev-server.sh start`
2. Open: http://localhost:8080
3. Chat through the web interface

## Dependencies

- **Anthropic Java SDK** 2.0.0 - Claude API integration
- **Google Gen AI Java SDK** 1.5.0 - Gemini API integration
- **Spring Boot** 3.5.0 - Web framework
- **JUnit 5** - Testing framework
- **Java 24** - Runtime platform

## Troubleshooting

### Common Issues

1. **Missing API Key**: Ensure `code_editing_agent_api_key` is set
2. **Java Version**: Requires Java 24 or higher
3. **Port Conflicts**: Web mode uses port 8080 by default
