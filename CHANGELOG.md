# Changelog

All notable changes to this project are documented here.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/); versions follow [SemVer](https://semver.org).

## [Unreleased]

## [5.0.1] - 2026-09-29

### Removed
- Dependency on Ultimate subscription

## [5.0.0] - 2026-09-27

### Changed
- **Minimum IDE is now 2026.1.4** (build 261.26222, was 2024.2): migrated to the `LspIntegrationProvider` LSP API, which replaces the deprecated `LspServerSupportProvider`
- **Java 21** is required to build (was 17); plugin bytecode targets Java 21
- Build: Gradle 9.8.0, Kotlin 2.4.20, IntelliJ Platform Gradle Plugin 2.19.0, Plugin Verifier checks IDE 2026.1.4 and 2026.2.3; JUnit 6.1.3; GitHub Actions bumped to latest majors

### Removed
- Unused test dependencies MockK, Kotest and kotlin-test-junit5

## [4.0.0] - 2026-07-13

### Added
- Warning notification when the configured bkmr binary cannot be found (LSP no longer fails silently)
- CI (GitHub Actions): build, unit tests, ktlint, IntelliJ Plugin Verifier; Dependabot for Gradle and Actions

### Changed
- Filepath comment insertion is shebang- and BOM-aware and never inserts a duplicate
- `.svg` is treated as a text file (XML comment syntax applies)
- Debug output goes to the IDE log (`idea.log`) instead of stdout
- `VERSION` file is the single source of truth for the plugin version
- Plugin zip no longer bundles the Kotlin stdlib (provided by the platform)

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
