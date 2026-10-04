---
name: upgrade-plugin
description: Upgrade the bkmr IntelliJ plugin's build and platform - Gradle, Kotlin, IntelliJ Platform Gradle Plugin, test libraries, GitHub Actions, ktlint, Java toolchain, and the supported IDE range (sinceBuild/untilBuild, Plugin Verifier IDEs) - including migrating off deprecated IntelliJ APIs. Use when asked to update dependencies, support a new IDE release, fix deprecation warnings, or prepare a maintenance release.
---

# Upgrade the bkmr IntelliJ plugin

Work on a branch, keep commits focused (deps / IDE range / API migration), open a PR. Don't bump
`VERSION` and don't tag: the maintainer releases locally with `make bump-*` (see "Release" below).

## 1. Find what's outdated

| What | Where | Latest version from |
|---|---|---|
| Gradle wrapper | `gradle/wrapper/gradle-wrapper.properties` | https://services.gradle.org/versions/current |
| Kotlin, IntelliJ Platform Gradle Plugin, ktlint-gradle, Gradle Changelog Plugin | `plugins {}` in `build.gradle.kts` | Gradle Plugin Portal / Maven Central metadata |
| ktlint engine | `ktlint { version.set(...) }` | GitHub releases of pinterest/ktlint |
| JUnit BOM | `build.gradle.kts` dependencies | Maven Central `org.junit:junit-bom` |
| GitHub Actions | `.github/workflows/build.yml` | each action's latest major tag |
| IDE releases | `intellijIdeaUltimate(...)`, `pluginVerification.ides` | `https://www.jetbrains.com/intellij-repository/releases` (Maven metadata for `com.jetbrains.intellij.idea:idea`) |

Upgrade Gradle first (`./gradlew wrapper --gradle-version <v>`, run twice so the wrapper scripts
update too), then plugins, then libraries. Read the IntelliJ Platform Gradle Plugin changelog for
renamed DSL.

## 2. Decide the IDE range

- `sinceBuild` (in `build.gradle.kts`, injected into `plugin.xml` by `patchPluginXml`) is the oldest
  supported IDE. **The main `intellijIdeaUltimate(...)` dependency must be that same version.**
  Compiling against a newer platform can make Kotlin emit stubs for interface methods the floor lacks,
  which fail with `NoSuchMethodError` there.
- When an API migration forces a higher floor, find the **exact first build** that ships the API, not
  just the year.minor: e.g. `LspIntegrationProvider` first appeared in 2026.1.4 (261.26222), not
  2026.1.0. Check by downloading candidate builds and looking for the class, or let the Plugin
  Verifier fail on the older one.
- Raising `sinceBuild` drops users on older IDEs: it's a breaking change (`feat!:` commit, CHANGELOG
  entry in bold). Ask the user before raising it.
- `untilBuild` is `<latest year.minor>.*`.
- `pluginVerification.ides`: the floor, the newest release, and PyCharm (newest) to cover an IDE that
  isn't IntelliJ IDEA. Use explicit versions with `useInstaller = false` (ZIP, no DMG mounting).
- Java: use the JBR version the floor IDE ships (2026.x = Java 21). Changing it touches
  `jvmToolchain`, `sourceCompatibility`/`targetCompatibility`, both Kotlin `jvmTarget`s, CI
  `setup-java`, the Makefile `JAVA_HOME` pin, README, and `.claude/hooks/session-start.sh`.

## 3. Fix deprecations and API changes

- Use the Plugin Verifier report as the to-do list: `make verify`, then read the console output (or
  `build/reports/pluginVerifier/<IDE>/report.html`) for "deprecated API usages",
  "experimental API usages" and "internal API usages". Also check compiler warnings (`w:` lines).
- Find the replacement in the platform sources (`@Deprecated` KDoc / `ReplaceWith`) or the IntelliJ
  Platform SDK docs. Rename classes when the API concept changes (e.g.
  `BkmrLspServerSupportProvider` -> `BkmrLspIntegrationProvider`) and update the `plugin.xml`
  extension point (e.g. `platform.lsp.serverSupportProvider` -> `platform.lsp.integrationProvider`).
- Keep `plugin.xml` `<depends>` on `com.intellij.modules.platform` and `com.intellij.modules.lsp`.

## 4. Remove what's unused

Check whether each test dependency is referenced in `src/test` before keeping it. Removing
`kotlin-test-junit5` dropped the Kotlin stdlib from the test classpath (stdlib default dependency is
off), so keep `testRuntimeOnly(kotlin("stdlib"))`.

## 5. Verify

```bash
./gradlew --console=plain --max-workers=1 ktlintCheck unitTest buildPlugin verifyPluginStructure verifyPlugin
```

Done when:
- no `w:` compiler warnings, ktlint clean, all unit tests pass
- every verifier IDE reports `Compatible` with no deprecated/experimental/internal API usages
- the built zip's `plugin.xml` has the expected `<version>`, `<idea-version since-build/until-build>`
  and change notes (`unzip -p build/distributions/*.zip '*/lib/*.jar' > p.jar; unzip -p p.jar META-INF/plugin.xml`)
- after a Java change: `javap -v` on a plugin class shows the expected `major version` (65 = Java 21)

Pitfalls seen before:
- Verifier IDE downloads fail with HTTP 429 (rate limit): wait ~1 minute and retry.
- Each IDE is 2-3 GB in `~/.gradle/caches`; delete versions no longer referenced before the disk fills.
- Run `make format` if ktlint fails on formatting only.

## 6. Document

- `CHANGELOG.md`: entries under `## [Unreleased]` (Added / Changed / Removed / Fixed). Don't create a
  version heading: `make bump-*` renames the Unreleased section to the new version and date. The
  plugin's change notes and the GitHub release notes are generated from these entries, so write them
  for users.
- README "Platform Compatibility" (supported IDEs, version range, JDK) when the range or Java changes.

Then push the branch, open the PR, and wait for CI (`build` and `verify` jobs).

## Release (maintainer, locally)

Cloud sessions can't push tags or create releases (HTTP 403), so hand these steps to the user:

1. `make bump-patch` (or `bump-minor` / `bump-major`): bumps `VERSION`, turns `## [Unreleased]` in
   `CHANGELOG.md` into `## [<version>] - <date>` (leaving an empty Unreleased heading above it),
   commits, tags, pushes, and creates the GitHub release with that section as its notes. Needs
   `GITHUB_TOKEN`. It aborts without changing anything if the `## [Unreleased]` heading is missing.
2. `make verify`, then `make publish` (needs `JETBRAINS_MARKETPLACE_TOKEN`). The plugin's change notes
   are rendered from the same CHANGELOG section at build time.
