# bkmr-intellij-plugin

IntelliJ Platform plugin for [bkmr](https://github.com/sysid/bkmr-lsp) snippet manager integration via Language Server Protocol (LSP).

## Features

- **Snippet Completion**: Trigger-based completion using colon (`:`) character
- **LSP Integration**: Seamless integration with bkmr-lsp server
- **Configurable Settings**: Binary path and debug logging options
- **Non-intrusive**: No interference with regular code completion

## Usage

1. Type `:` followed by letters to trigger snippet completion
2. Examples: `:hello`, `:snip:java`, `:s:test`
3. Use Ctrl+Space for manual completion in snippet contexts
4. Navigate with arrow keys, press Tab or Enter to complete

## Requirements

- bkmr-lsp binary available in PATH or configured in settings
- bkmr command-line tool with snippets configured

## Development

### Building

```bash
# Build the plugin
./gradlew buildPlugin

# Run IDE with plugin in sandbox
./gradlew runIde

# Run tests  
./gradlew test

# Clean build artifacts
./gradlew clean
```

### Distribution

```bash
# Sign plugin (requires certificates)
./gradlew signPlugin

# Publish to JetBrains Marketplace (requires token)
./gradlew publishPlugin
```

### Version Management

```bash
# Bump version (requires bump-my-version)
bump-my-version bump patch   # 1.0.0 → 1.0.1
bump-my-version bump minor   # 1.0.0 → 1.1.0  
bump-my-version bump major   # 1.0.0 → 2.0.0
```

### Debugging

```bash
# View plugin logs of sandbox
make log-plugin

# View LSP server logs
make log-lsp

# Initialize development environment
make init
```

## Configuration

Access plugin settings via **File → Settings → Tools → bkmr**:

- **Enable LSP Integration**: Toggle LSP-based completion
- **Binary Path**: Path to bkmr-lsp executable
- **Debug Logging**: Enable detailed logging for troubleshooting

## Installation

1. Download from [JetBrains Marketplace](https://plugins.jetbrains.com)
2. Or install manually: **File → Settings → Plugins → Install Plugin from Disk**
3. Configure bkmr-lsp binary path in settings if not in PATH

## Documentation

- [LSP Integration Guide](https://plugins.jetbrains.com/docs/intellij/language-server-protocol.html)
- [Plugin Development](https://plugins.jetbrains.com/docs/intellij/developing-plugins.html)

## Gotcha
- completion only works in project context (scratch  files are ignored)
