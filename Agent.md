# Code Editing Agent Java

## Commands

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

## Logging

- **CLI logs**: `logs/application-cli.log` (separate file for CLI mode)
- **Web logs**: `logs/application-web.log` (separate file for web mode)
- **Log rotation**: Daily rotation with 30-day retention and 100MB size cap
- **Clean startup**: No verbose Logback configuration messages in CLI mode

## Development Server

For web development with hot reloading:
- **Start**: `./dev-server.sh start` (web mode with hot reloading)
- **Stop**: `./dev-server.sh stop`
- **Restart**: `./dev-server.sh restart` (needed for static file changes)
- **Status**: `./dev-server.sh status`
- **Logs**: `./dev-server.sh logs`

**Note**: Hot reloading works for Java code changes. Static files (HTML, CSS, JS) in `src/main/resources/static/` require being "built" (copied to classpath) to trigger live reload - when running from command line with Gradle, this typically requires a server restart to see changes.

## Code Style Guidelines
- **Package**: Use `com.larseckart` root package
- **Imports**: Group imports (standard library, third-party, then local), alphabetically within groups
- **Classes**: PascalCase (e.g., `ReadFileTool`, `ToolDefinition`)
- **Methods/Variables**: camelCase
- **Constants**: UPPER_SNAKE_CASE
- **Indentation**: 2 spaces (as shown in existing code)
- **Braces**: Opening brace on same line
- **Testing**: Use JUnit 5 (`@Test`, no public methods needed)
- **Dependencies**: Add to `gradle/libs.versions.toml` first, reference in build.gradle.kts
- **Main class**: `com.larseckart.App`
- **Java version**: 24 (configured in toolchain)

## Testing Conventions
- **Test classes**: End with `Test` suffix (e.g., `ReadFileToolTest`)
- **Display names**: Use `@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)`
- **Test methods**: Use snake_case format (e.g., `should_handle_empty_file()`)
- **Error tests**: Use `should_throw_exception_for_*` pattern
- **Validation tests**: Use `should_validate_required_parameters()` pattern
- **Platform-specific**: Use `@DisabledOnOs(OS.WINDOWS)` instead of manual OS detection

## Architecture
- **Core**: Business logic in `core/` package (services, domain, ports)
- **Adapters**: I/O implementations in `adapters/` (cli, web)
- **Tools**: File operations in `core/tools/` (ReadFileTool, EditFileTool, ListFilesTool)
- **Hexagonal Architecture**: Clear separation between business logic and I/O

## Key Dependencies
- **Anthropic Java SDK**: `com.anthropic:anthropic-java:2.0.0`
- **Spring Boot**: `3.5.0` (for web mode)
- **JUnit 5**: For testing
- **Jackson**: For JSON handling

## Environment Variables
- **API Key**: `code_editing_agent_api_key` - Required for Claude API access
- **App Mode**: `app.mode=web` - Optional, defaults to CLI mode

## Git Commits
- **Co-author**: Add yourself as co-author to all commits we make using:
  ```
  Co-authored-by: Amp <amp@sourcegraph.com>
  ```