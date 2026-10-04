# bkmr IntelliJ plugin

IntelliJ Platform plugin (Kotlin) that connects IDEs to the `bkmr lsp` language server and adds an
"Insert Filepath Comment" action. Supports IDE builds 2026.1.4 (261.26222) through 2026.2.x.

For dependency, IDE-range or toolchain upgrades, use the `upgrade-plugin` skill (`.claude/skills/upgrade-plugin`).

## Commands

```bash
make lint       # ktlint (engine pinned in build.gradle.kts)
make test       # unit tests: ./gradlew unitTest (plain JUnit, no IDE)
make check      # lint + compile + test + build: what CI's build job runs
make verify     # verifyPluginStructure + Plugin Verifier: downloads IDEs, slow
```

## Rules

- **`VERSION` is the single source of truth** for the version. Don't bump it in a PR: the maintainer
  releases with `make bump-patch|minor|major` locally, which bumps, tags, pushes and creates the
  GitHub release. Put changes under `## [Unreleased]` in `CHANGELOG.md`.
- **Plugin change notes** (`changeNotes` in `build.gradle.kts`) are shown in the IDE's plugin manager;
  update them with each user-visible change.
- **`plugin.xml` must keep `<depends>com.intellij.modules.platform</depends>` and
  `<depends>com.intellij.modules.lsp</depends>`.** Without any `<depends>`, Marketplace rejects the
  plugin or treats it as IntelliJ IDEA-only. Never depend on `com.intellij.modules.ultimate`: the LSP API
  is available without a subscription.
- **Compile against the `sinceBuild` floor**, not the newest IDE: compiling against a newer platform
  can emit stubs for interface methods the floor lacks (`NoSuchMethodError` at runtime there).
- **`kotlin.stdlib.default.dependency = false`** (the IDE provides the stdlib at runtime), so unit tests
  need `testRuntimeOnly(kotlin("stdlib"))`. Don't remove it.
- Java 21 toolchain (`jvmToolchain(21)`); the Makefile pins `JAVA_HOME` to JDK 21.

## Claude Code on the web

- The SessionStart hook (`.claude/hooks/session-start.sh`) installs JDK 21.
- Sessions can push branches, open and merge PRs, but **cannot push tags or create releases** (HTTP 403).
  Releasing is done by the maintainer locally.
- Downloaded IDEs (~2-3 GB each) live in `~/.gradle/caches`. On "No space left on device", delete IDE
  versions no longer referenced in `build.gradle.kts` from `modules-2/files-2.1/com.jetbrains.intellij.*`
  and `<gradle-version>/transforms`.
- Plugin Verifier IDE downloads sometimes fail with HTTP 429: wait a minute and retry.
- Run Gradle with `--max-workers=1` to keep memory use low.
