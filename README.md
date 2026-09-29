# bkmr-intellij-plugin

[![Build](https://github.com/sysid/bkmr-intellij-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/sysid/bkmr-intellij-plugin/actions/workflows/build.yml)

IntelliJ Platform plugin for [bkmr](https://github.com/sysid/bkmr) snippet manager integration via Language Server Protocol (LSP).

## Features

- **Automatic Snippet Completion**: Snippets appear in completion popup while typing (no trigger characters needed)
- **Tab Navigation**: Navigate through snippet placeholders with Tab/Shift+Tab
- **LSP Integration**: Uses bkmr's built-in LSP server (`bkmr lsp`)
- **Filepath Comment Insertion**: Insert the relative filepath as a comment at the beginning of the file
- **Smart Comment Detection**: Automatic comment syntax detection for 20+ file types
- **Configurable Settings**: Binary path and debug logging options

## Usage

### Snippet Completion

1. **Automatic completion**: Snippets appear in completion popup while typing
2. **Manual completion**: Use Ctrl+Space to trigger completion manually
3. **Snippet navigation**: Use Tab/Shift+Tab to move through placeholder fields

**Example**: Type `hello` and see matching snippets in the completion popup.

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
1. Calculates the relative path from the project base directory to the current file
   (falls back to the filename for files outside the project)
2. Automatically selects the appropriate comment syntax based on file extension
3. Inserts the comment at the beginning of the file — after a `#!` shebang line or byte-order
   mark if present, and never twice (repeated invocations are no-ops)

## Requirements

### For Snippet Completion
- **bkmr command-line tool**: Version 4.24.0+ with built-in LSP server (`bkmr lsp` command)
- Snippets must be configured in bkmr with `--interpolate` support

### For Filepath Comment Insertion
- **No additional requirements**: This feature works independently of the LSP server

### Platform Compatibility
- **Supported IDEs**: IntelliJ-based IDEs that ship the LSP API (IntelliJ IDEA, PyCharm, RustRover,
  GoLand, WebStorm, ...), with or without a subscription. The plugin depends on `com.intellij.modules.lsp`.
- **Version Range**: IntelliJ Platform 2026.1.4 (build 261.26222) through 2026.2
- **JDK Requirement**: Java 21 or higher

## Development

### Building

```bash
# Build the plugin
make build            # or: ./gradlew buildPlugin

# Run IDE with plugin in sandbox
make test-ide         # or: ./gradlew runIde

# Run unit tests (custom task — the default `test` task is disabled, see build.gradle.kts)
make test             # or: ./gradlew unitTest

# Check / fix code style (ktlint)
make lint
make format

# Verify plugin descriptor and compatibility with the target IDEs (Plugin Verifier, needs network)
make verify           # or: ./gradlew verifyPluginStructure verifyPlugin

# Run everything CI runs
make check
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

## Releasing

Version is managed in the `VERSION` file (single source of truth — `build.gradle.kts` reads it,
`patchPluginXml` injects it into the plugin manifest).

```bash
# 1. Update CHANGELOG.md and the changeNotes block in build.gradle.kts
# 2. Bump, tag, push and create the GitHub release (requires GITHUB_TOKEN):
make bump-patch       # or bump-minor / bump-major

# 3. Verify, then publish to JetBrains Marketplace (requires JETBRAINS_MARKETPLACE_TOKEN):
make verify  # takes time
make publish
```

CI (GitHub Actions) builds, lints, tests and runs the IntelliJ Plugin Verifier on every push
and PR to `main`. Publishing is deliberately manual.

## Configuration

Access plugin settings via **File → Settings → Tools → bkmr**:

- **Enable LSP Integration**: Toggle LSP-based completion (affects snippet completion only)
- **Binary Path**: Path to bkmr executable (required for snippet completion)
- **Debug Logging**: Enable detailed logging for troubleshooting

**Note**: The filepath comment insertion feature works independently of these settings and doesn't require LSP server configuration.

## Installation

1. Download from [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/27710-bkmr)
2. Or install manually: **File → Settings → Plugins → Install Plugin from Disk**
3. **Optional**: Configure bkmr binary path in settings for snippet completion

**Quick Start**: After installation, you can immediately use the filepath comment feature via `Cmd+Shift+A` → "Bkmr: Insert Filepath Comment".

## Documentation

- [LSP Integration Guide](https://plugins.jetbrains.com/docs/intellij/language-server-protocol.html)
- [Plugin Development](https://plugins.jetbrains.com/docs/intellij/developing-plugins.html)

## Gotchas & Known Issues

### Snippet Completion
- Completion only works in project context (scratch files are ignored)
- Requires the bkmr LSP server to be running and properly configured
- If the configured bkmr binary cannot be found, the plugin shows a warning notification and
  the LSP server is not started

### Filepath Comment Insertion
- Works with all text files, including scratch files
- Binary files (images, executables, etc.) are automatically excluded
- Files outside the project fall back to showing just the filename

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
