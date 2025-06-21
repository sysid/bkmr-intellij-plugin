# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a multi-project repository containing:
- **bkmr-intellij-plugin**: IntelliJ Platform plugin for bkmr snippet manager integration
- **bkmr-lsp**: Rust-based Language Server Protocol implementation for bkmr

The plugin provides LSP-based snippet completion triggered by the colon character (`:`) in JetBrains IDEs.

## Development Commands

### IntelliJ Plugin Development

```bash
# Build the plugin
./gradlew buildPlugin

# Run IDE with plugin in sandbox for testing
./gradlew runIde

# Test the plugin
./gradlew test

# Sign plugin for distribution
./gradlew signPlugin

# Publish plugin to marketplace
./gradlew publishPlugin

# Clean build artifacts
./gradlew clean
```

### LSP Server Development (from bkmr-lsp directory)

```bash
# Build LSP server
make build            # Release build
make build-fast       # Debug build

# Install LSP binary
make install          # Install release version
make install-debug    # Install debug version

# Run tests
make test

# Format and lint code
make format
make lint

# Clean build artifacts
make clean
```

### Development Workflow

```bash
# Initialize development environment
make init

# Test plugin in sandbox IDE
make test  # From plugin directory

# View plugin logs
make log-plugin

# View LSP server logs  
make log-lsp
```

## Architecture

### Plugin Architecture

- **BkmrLspServerSupportProvider**: Manages LSP server lifecycle and file associations
- **BkmrLspServerDescriptor**: Configures LSP server command line and initialization
- **BkmrSettings**: Persistent application-level settings for binary path and debug options
- **BkmrConfigurable**: Settings UI for the plugin configuration

### LSP Integration

The plugin uses IntelliJ Platform's LSP API (`com.intellij.platform.lsp.api`) to:
- Start the bkmr-lsp server automatically when text files are opened
- Provide completion triggered by the colon (`:`) character
- Filter file types to avoid binary files and inappropriate contexts

### Key Components

1. **Settings Management**: Uses IntelliJ's `PersistentStateComponent` for configuration persistence
2. **File Type Filtering**: Supports text files while excluding binary formats (images, executables, etc.)
3. **LSP Server Lifecycle**: Manages server startup per project with proper working directory setup
4. **Environment Configuration**: Sets RUST_LOG for debug logging when enabled

### Configuration

- Settings stored in `bkmr.xml` at application level
- Configurable binary path with OS-specific defaults
- Debug logging toggle for development
- LSP integration can be disabled via settings

## Build System

- **Gradle**: Uses IntelliJ Platform Gradle Plugin 2.6.0
- **Kotlin**: Version 2.1.21 with JVM target 17
- **IntelliJ Platform**: Targets IntelliJ IDEA Ultimate 2025.1.1.1
- **LSP Server**: Rust-based using tower-lsp framework

## Development Requirements

- JDK 17 or higher
- Rust toolchain for LSP server development
- bkmr-lsp binary must be available in PATH or configured in settings
- IntelliJ IDEA with Plugin Development Kit for plugin development