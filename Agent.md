# Code Editing Agent Java

## Commands
- **Build**: `./gradlew build` (includes fatJar)
- **Test**: `./gradlew test`
- **Single test**: `./gradlew test --tests "ClassName"`
- **Run**: `./gradlew run`
- **Clean**: `./gradlew clean`

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
- **Java version**: 21 (configured in toolchain)