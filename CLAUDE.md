# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Roq-idea is an IntelliJ Platform plugin built using the JetBrains IntelliJ Platform Plugin Template. The plugin is currently in early development (v0.0.1) and targets IntelliJ IDEA 2025.2+ (build 252+).

The goal of the plugin is to provide native IDE support for the Roq static site generator (https://iamroq.com). The plugin should allow users to:
- Generate a new Row project
- Add Roq plugins to the project, handling the updating the Maven or Gradle build files on the user's behalf
- Add new data files to `data/`
- Add new layouts to `templates/layouts`
- Add new partials to `templates/partials`
- Run the site, using Quarkus' dev mode, to allow the user to preview changes
- Produce a production build of the site uploading, archiving, etc.

**Plugin Identification:**
- Plugin ID: `com.steeplesoft`
- Package: `com.steeplesoft.roq-idea`
- Repository: https://github.com/jasondlee/roq-idea

## Build and Test Commands

### Development Workflow
```bash
# Build the plugin
./gradlew build

# Build the plugin ZIP for distribution
./gradlew buildPlugin

# Run IntelliJ IDEA with the plugin loaded
./gradlew runIde

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.steeplesoft.roq-idea.MyPluginTest"

# Verify plugin compatibility with target IDE versions
./gradlew verifyPlugin

# Verify plugin project configuration
./gradlew verifyPluginProjectConfiguration
```

### Code Quality and Coverage
```bash
# Run Qodana code quality analysis
./gradlew qodana

# Generate code coverage report with Kover
./gradlew koverHtmlReport
```

### Release Tasks
```bash
# Patch CHANGELOG.md with unreleased changes
./gradlew patchChangelog

# Build searchable options index for plugin
./gradlew buildSearchableOptions

# Publish plugin to JetBrains Marketplace (requires PUBLISH_TOKEN)
./gradlew publishPlugin
```

## Architecture

### Plugin Components

The plugin follows the standard IntelliJ Platform plugin architecture:

1. **Tool Window** (`MyToolWindowFactory.kt`): Registers a custom tool window that displays a simple UI with a button to generate random numbers via the project service.

2. **Startup Activity** (`MyProjectActivity.kt`): Executes actions when a project is opened. Registered in `plugin.xml` as a `postStartupActivity`.

3. **Project Service** (`MyProjectService.kt`): A project-level service that provides functionality (e.g., random number generation in the template). Services are registered and accessed via IntelliJ's dependency injection.

4. **Resource Bundle** (`MyBundle.kt`): Handles internationalization (i18n) using message properties files located in `src/main/resources/messages/`.

### Configuration Files

- **`gradle.properties`**: Contains all plugin metadata including version, target platform version, and build configuration. When updating plugin version or target IDE version, edit this file.

- **`src/main/resources/META-INF/plugin.xml`**: The plugin descriptor that defines:
  - Plugin ID and metadata
  - Dependencies on IntelliJ Platform modules
  - Extension points and extensions (tool windows, startup activities)
  - Resource bundle location

- **`README.md`**: The plugin description is extracted from the `<!-- Plugin description -->` section during build. This description appears in the JetBrains Marketplace.

- **`CHANGELOG.md`**: Managed by the Gradle Changelog Plugin. Change notes are automatically extracted and included in the plugin distribution.

### Package Structure

```
com.steeplesoft.roq-idea/
├── services/          # Project and application services
├── startup/           # Startup activities
├── toolWindow/        # Tool window factories and UI
└── MyBundle.kt        # i18n resource bundle
```

Note: The package was recently migrated from `com.github.jasondlee.roq-idea` to `com.steeplesoft.roq-idea`. Ensure all new code uses the new package structure.

## Development Notes

### JVM and Kotlin
- The project uses JVM 21 toolchain
- Kotlin stdlib is explicitly not bundled (`kotlin.stdlib.default.dependency = false`)
- Gradle configuration cache and build cache are enabled for faster builds

### Testing
- Tests use JUnit and the IntelliJ Platform Test Framework
- Tests are located in `src/test/kotlin/` mirroring the main source structure
- The test framework provides `TestFrameworkType.Platform` for testing against the IntelliJ Platform

### Plugin Description
The plugin description shown in the marketplace is extracted from README.md between the `<!-- Plugin description -->` and `<!-- Plugin description end -->` markers. Do not remove these markers.

### UI Testing
The build includes a special `runIdeForUiTests` task configured with the Robot Server Plugin for automated UI testing:
```bash
./gradlew runIdeForUiTests
```

### Sandbox Environment
IntelliJ Platform plugins run in a sandbox during development:
- `prepareSandbox`: Sets up the plugin in the sandbox
- `cleanSandbox`: Cleans the sandbox environment
- Sandbox location is managed by the IntelliJ Platform Gradle Plugin

## Common Development Patterns

### Accessing Services
```kotlin
// Project-level service
val service = project.service<MyProjectService>()

// Application-level service
val service = service<MyApplicationService>()
```

### Logging
Use `thisLogger()` from the IntelliJ Platform API:
```kotlin
import com.intellij.openapi.diagnostic.thisLogger

thisLogger().info("Message")
thisLogger().warn("Warning")
```

### Resource Bundles
Access localized messages via the bundle:
```kotlin
MyBundle.message("key", args)
```

## Release Workflow

1. Update version in `gradle.properties` (follow SemVer)
2. Update `CHANGELOG.md` with changes
3. Run `./gradlew patchChangelog` to move unreleased changes to the version section
4. Build and verify: `./gradlew buildPlugin verifyPlugin`
5. Test the plugin: `./gradlew runIde`
6. Publish: `./gradlew publishPlugin` (requires `PUBLISH_TOKEN` environment variable)

## Environment Variables for CI/CD

- `CERTIFICATE_CHAIN`: Plugin signing certificate chain
- `PRIVATE_KEY`: Plugin signing private key
- `PRIVATE_KEY_PASSWORD`: Password for the private key
- `PUBLISH_TOKEN`: JetBrains Marketplace publishing token
- `CODECOV_TOKEN`: Codecov token for test coverage reports (optional)
