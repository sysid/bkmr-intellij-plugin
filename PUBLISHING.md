### 🛠️ 1. Compile & Build

```bash
./gradlew buildPlugin
```

This compiles code, instruments forms, and packages the plugin ZIP in `build/distributions/` ([plugins.jetbrains.com][1], [docs.gradle.org][2], [plugins.jetbrains.com][3]).

### ✅ 2. Testing
For manual plugin validation, launch sandbox with:

```bash
./gradlew runIde
```

You can inspect your plugin in the sandbox instance .

### ✍️ 3. Sign Plugin
Before distribution, you **must sign** the plugin:

```bash
./gradlew signPlugin
```

Ensure:

* You have a valid certificate or use JetBrains Marketplace signing.
* `signPlugin` runs automatically before `publishPlugin` ([plugins.jetbrains.com][3]).

### 📤 4. Distribute
#### A. Manual Upload

* Locate `build/distributions/your-plugin.zip`.
* Go to Marketplace → **Add new plugin** → upload.

#### B. Automated via Gradle
Generate a **Personal Access Token** (Marketplace account).

```bash
export ORG_GRADLE_PROJECT_intellijPlatformPublishingToken=YOUR_TOKEN
./gradlew publishPlugin
```

This:

* Builds, signs, and publishes in one step.
* Can target channels (e.g., beta):

```kotlin
intellij {
  publishing {
    channels.set(listOf("beta"))
  }
}
```

([plugins.jetbrains.com][3], [github.com][4])

### 🔁 5. CI/CD & Custom Servers

* For JetBrains IDE Services (private plugin repo): configure Gradle's `publishPlugin` with `host`, `token`, and `channels` ([jetbrains.com][5]).
* Use `curl` for lightweight uploads:

```bash
curl -i \
  -H "Authorization: Automation $TOKEN" \
  -F "file=@path/to/plugin.zip" \
  -F "channel=Stable" \
  https://your-server/api/plugins
```

### ✅ Final Workflow Summary

| Step                | Command                                                  |
| ------------------- | -------------------------------------------------------- |
| Build               | `./gradlew buildPlugin`                                  |
| Run tests           | `./gradlew test` or `./gradlew runIde`                   |
| Sign                | `./gradlew signPlugin`                                   |
| Publish (manual)    | Upload via Marketplace UI                                |
| Publish (automated) | `./gradlew publishPlugin` (with token, optional channel) |

Following this gives you a fully compiled, tested, signed, and distributable IntelliJ plugin using the latest Gradle tooling.

[1]: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html?utm_source=chatgpt.com "Gradle IntelliJ Plugin (1.x) - JetBrains Marketplace"
[2]: https://docs.gradle.org/current/userguide/idea_plugin.html?utm_source=chatgpt.com "The IDEA Plugin - Gradle User Manual"
[3]: https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?utm_source=chatgpt.com "Publishing a Plugin | IntelliJ Platform Plugin SDK"
[4]: https://github.com/JetBrains/intellij-platform-gradle-plugin/issues/1687?utm_source=chatgpt.com "Publishing a plugin with the Maven Publish plugin do not take into ..."
[5]: https://www.jetbrains.com/help/ide-services/manage-plugin-repository.html?utm_source=chatgpt.com "Private plugin repository | IDE Services Documentation - JetBrains"