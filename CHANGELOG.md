# Changelog

All notable changes to this project are documented here.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/); versions follow [SemVer](https://semver.org).

## [Unreleased]

### Added
- Warning notification when the configured bkmr binary cannot be found (LSP no longer fails silently)
- CI (GitHub Actions): build, unit tests, ktlint, IntelliJ Plugin Verifier; Dependabot for Gradle and Actions

### Changed
- Filepath comment insertion is shebang- and BOM-aware and never inserts a duplicate
- `.svg` is treated as a text file (XML comment syntax applies)
- Debug output goes to the IDE log (`idea.log`) instead of stdout
- `VERSION` file is the single source of truth for the plugin version
- Plugin zip no longer bundles the Kotlin stdlib (provided by the platform)
- Build: Gradle 9.8.0, Kotlin 2.4.20, IntelliJ Platform Gradle Plugin 2.19.0, Plugin Verifier now also checks IDE 2026.2.3; JUnit 6.1.3, MockK 1.14.11, Kotest 6.2.5; GitHub Actions bumped to latest majors

## [3.0.0] - 2025-08-24

### Changed
- Use the consolidated `bkmr lsp` command — the separate `bkmr-lsp` binary is no longer needed
- Fixed deprecation warnings

## [2.0.0] - 2025-08-17

### Changed
- Automatic completion — snippets appear while typing, no trigger character needed
- Enhanced platform compatibility

## [1.1.0] - 2025-06-21

### Added
- Tab navigation through snippet placeholders

## [1.0.1] - 2025-06-21

### Added
- Insert Filepath Comment action with automatic comment syntax detection
- Context menu and Tools menu integration

## [1.0.0] - 2025-06

### Added
- Initial release: trigger-based snippet completion via bkmr-lsp
- Settings UI (binary path, LSP toggle, debug logging)
