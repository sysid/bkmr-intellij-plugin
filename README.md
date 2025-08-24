# bkmr-intellij-plugin

IntelliJ Platform plugin for [bkmr](https://github.com/sysid/bkmr) snippet manager integration via Language Server Protocol (LSP).

## Features

- **Automatic Snippet Completion**: Snippets appear in completion popup while typing (no trigger characters needed)
- **Tab Navigation**: Navigate through snippet placeholders with Tab/Shift+Tab (full snippet support)
- **Language-Aware Filtering**: Snippets automatically filtered by file type (Rust, Python, JavaScript, etc.)
- **Universal Snippets**: Language-agnostic snippets with automatic syntax translation
- **Cross-Platform Compatibility**: Works across all JetBrains IDEs (IntelliJ IDEA, PyCharm, WebStorm, CLion, etc.)
- **LSP Integration**: Seamless integration with bkmr's built-in LSP server
- **Filepath Comment Insertion**: Automatically insert filepath as comment at file beginning
- **Smart Comment Detection**: Automatic comment syntax detection for 20+ file types
- **Configurable Settings**: Binary path and debug logging options

## Usage

### Snippet Completion

1. **Automatic completion**: Snippets appear in completion popup while typing
2. **Manual completion**: Use Ctrl+Space to trigger completion manually
3. **Snippet navigation**: Use Tab/Shift+Tab to move through placeholder fields
4. **Language filtering**: Snippets are automatically filtered by current file type
5. **Universal snippets**: Language-agnostic snippets adapt to your current language syntax

**Examples**:
- Type `hello` and see matching snippets in completion popup
- Type `for` and get language-specific loop snippets
- Universal snippets automatically translate `// comment` to `# comment` in Python files

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
- **bkmr command-line tool**: Version 4.24.0+ with built-in LSP server (`bkmr lsp` command)
- Snippets must be configured in bkmr with `--interpolate` support

### For Filepath Comment Insertion
- **No additional requirements**: This feature works independently of the LSP server
- Works with any text file in any IntelliJ Platform IDE

### Platform Compatibility
- **Supported IDEs**: All JetBrains IDEs (IntelliJ IDEA, PyCharm, WebStorm, CLion, Rider, etc.)
- **Version Range**: IntelliJ Platform 2023.2+ through 2026.2
- **JDK Requirement**: Java 17 or higher

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
- **Binary Path**: Path to bkmr executable (required for snippet completion)
- **Debug Logging**: Enable detailed logging for troubleshooting

**Note**: The filepath comment insertion feature works independently of these settings and doesn't require LSP server configuration.

## Installation

1. Download from [JetBrains Marketplace](https://plugins.jetbrains.com)
2. Or install manually: **File → Settings → Plugins → Install Plugin from Disk**
3. **Optional**: Configure bkmr binary path in settings for snippet completion

**Quick Start**: After installation, you can immediately use the filepath comment feature via `Cmd+Shift+A` → "Bkmr: Insert Filepath Comment".

## Documentation

- [LSP Integration Guide](https://plugins.jetbrains.com/docs/intellij/language-server-protocol.html)
- [Plugin Development](https://plugins.jetbrains.com/docs/intellij/developing-plugins.html)

## Gotchas & Known Issues

### Snippet Completion
- Completion only works in project context (scratch files are ignored)
- Requires bkmr LSP server to be running and properly configured
- LSP server won't start if bkmr binary is not found in PATH or settings

### Filepath Comment Insertion
- Works with all text files, including scratch files
- Binary files (images, executables, etc.) are automatically excluded
- If no project root is detected, falls back to showing just the filename

## Troubleshooting

### Snippet Completion Not Working
1. Check that bkmr is installed with LSP support: `bkmr --version`
2. Verify bkmr has snippets: `bkmr search -t _snip_`
3. Test manual completion: Type text and press Ctrl+Space to see if snippets appear
4. Check plugin settings: **File → Settings → Tools → bkmr**
5. Enable debug logging in settings and check IDE logs

### Filepath Comment Action Not Visible
1. Ensure you have a file open in the editor
2. Try pressing `Cmd+Shift+A` and search for "bkmr"
3. Check that the file type is supported (not a binary file)
