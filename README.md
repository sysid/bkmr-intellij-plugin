# bkmr-intellij-plugin

IntelliJ Platform plugin for [bkmr](https://github.com/sysid/bkmr-lsp) snippet manager integration via Language Server Protocol (LSP).

## Features

- **Snippet Completion**: Trigger-based completion using colon (`:`) character
- **LSP Integration**: Seamless integration with bkmr-lsp server
- **Filepath Comment Insertion**: Automatically insert filepath as comment at file beginning
- **Smart Comment Detection**: Automatic comment syntax detection for 20+ file types
- **Configurable Settings**: Binary path and debug logging options
- **Non-intrusive**: No interference with regular code completion

## Usage

### Snippet Completion

1. Type `:` followed by letters to trigger snippet completion
2. Examples: `:hello`, `:snip:java`, `:s:test`
3. Use Ctrl+Space for manual completion in snippet contexts
4. Navigate with arrow keys, press Tab or Enter to complete

### Filepath Comment Insertion

Insert the relative filepath as a comment at the beginning of any file:

1. **Via Action Menu**: Press `Cmd+Shift+A` and search for "Bkmr: Insert Filepath Comment"
2. **Via Right-click Menu**: Right-click in editor, tab, or project view and select "Insert Filepath Comment"
3. **Via Tools Menu**: Navigate to Tools → Insert Filepath Comment

**Supported File Types** (with automatic comment syntax):
- **C-style**: `.rs`, `.java`, `.js`, `.ts`, `.cpp`, `.go`, `.swift`, `.kt` → `//`
- **Shell-style**: `.py`, `.sh`, `.yaml`, `.toml`, `.rb`, `.pl` → `#`
- **HTML/XML**: `.html`, `.xml`, `.svg` → `<!-- -->`
- **CSS**: `.css`, `.scss`, `.sass` → `/* */`
- **SQL**: `.sql` → `--`
- **And many more...**

**Example Output**:
```rust
// src/main.rs
fn main() {
    println!("Hello, world!");
}
```

**How it Works:**
1. Detects the project root by looking for common indicators (`Cargo.toml`, `package.json`, `.git`, `Makefile`, etc.)
2. Calculates the relative path from project root to the current file
3. Automatically selects the appropriate comment syntax based on file extension
4. Inserts the comment at the very beginning of the file (line 1, character 1)

**Benefits:**
- **Code Organization**: Easily identify file locations in large codebases
- **Documentation**: Self-documenting code with clear file references
- **Navigation**: Quick visual reference for file structure
- **Debugging**: Helpful when reviewing code snippets or error logs

## Requirements

### For Snippet Completion
- **bkmr-lsp binary**: Available in PATH or configured in plugin settings
- **bkmr command-line tool**: Version 4.24.0+ with snippets configured

### For Filepath Comment Insertion
- **No additional requirements**: This feature works independently of the LSP server
- Works with any text file in any IntelliJ Platform IDE

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

- **Enable LSP Integration**: Toggle LSP-based completion (affects snippet completion only)
- **Binary Path**: Path to bkmr-lsp executable (required for snippet completion)
- **Debug Logging**: Enable detailed logging for troubleshooting

**Note**: The filepath comment insertion feature works independently of these settings and doesn't require LSP server configuration.

## Installation

1. Download from [JetBrains Marketplace](https://plugins.jetbrains.com)
2. Or install manually: **File → Settings → Plugins → Install Plugin from Disk**
3. **Optional**: Configure bkmr-lsp binary path in settings for snippet completion

**Quick Start**: After installation, you can immediately use the filepath comment feature via `Cmd+Shift+A` → "Bkmr: Insert Filepath Comment".

## Documentation

- [LSP Integration Guide](https://plugins.jetbrains.com/docs/intellij/language-server-protocol.html)
- [Plugin Development](https://plugins.jetbrains.com/docs/intellij/developing-plugins.html)

## Gotchas & Known Issues

### Snippet Completion
- Completion only works in project context (scratch files are ignored)
- Requires bkmr-lsp server to be running and properly configured
- LSP server won't start if bkmr-lsp binary is not found in PATH or settings

### Filepath Comment Insertion
- Works with all text files, including scratch files
- Binary files (images, executables, etc.) are automatically excluded
- If no project root is detected, falls back to showing just the filename

## Troubleshooting

### Snippet Completion Not Working
1. Check that bkmr-lsp is installed: `which bkmr-lsp`
2. Verify bkmr has snippets: `bkmr search -t _snip_`
3. Check plugin settings: **File → Settings → Tools → bkmr**
4. Enable debug logging in settings and check IDE logs

### Filepath Comment Action Not Visible
1. Ensure you have a file open in the editor
2. Try pressing `Cmd+Shift+A` and search for "bkmr"
3. Check that the file type is supported (not a binary file)
