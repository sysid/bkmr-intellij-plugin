import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.20"
    id("org.jetbrains.intellij.platform") version "2.19.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

group = "com.sysid"
// Single source of truth for the plugin version; bumped by bump-my-version (see .bumpversion.toml)
version = providers.fileContents(layout.projectDirectory.file("VERSION")).asText.get().trim()

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // useInstaller = false: fetch the ZIP distribution instead of the OS installer (DMG needs
        // hdiutil mounting, which fails in sandboxed/headless environments; ZIP works everywhere)
        // Compile against the sinceBuild floor (2026.1.4 = first build with LspIntegrationProvider):
        // compiling against a newer platform can make Kotlin emit delegating stubs for interface
        // methods the floor lacks, which fail with NoSuchMethodError there.
        intellijIdeaUltimate("2026.1.4") { useInstaller = false }
        pluginVerifier()
    }

    // Unit test dependencies (no platform dependencies)
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // Gradle 9 no longer puts the JUnit Platform launcher on the test runtime classpath
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // kotlin.stdlib.default.dependency=false (the IDE provides it at runtime), and unitTest runs
    // without the platform, so tests need the stdlib explicitly
    testRuntimeOnly(kotlin("stdlib"))
}

ktlint {
    // Pin the ktlint engine so local runs and CI enforce identical rules
    version.set("1.8.0")
}

intellijPlatform {
    // Disabled: buildSearchableOptions launches a headless IDE to index the settings panel for
    // the Settings search box. That IDE acquires a directory lock by binding a Unix-domain socket,
    // which fails with EPERM in sandboxed/restricted environments (e.g. `make publish`), aborting
    // the build. The plugin has a single settings field (bkmr binary path), so losing settings-search
    // indexing is an acceptable trade for a publish pipeline that runs anywhere. The panel still works.
    buildSearchableOptions = false

    pluginVerification {
        ides {
            // sinceBuild floor + current release of IntelliJ IDEA, plus PyCharm to cover a non-IDEA
            // IDE (the plugin only needs com.intellij.modules.lsp, not an IDEA-specific module).
            // useInstaller = false for the same reason as the main dependency (ZIP, no hdiutil).
            create(IntelliJPlatformType.IntellijIdeaUltimate, "2026.1.4") { useInstaller = false }
            create(IntelliJPlatformType.IntellijIdeaUltimate, "2026.2.3") { useInstaller = false }
            create(IntelliJPlatformType.PyCharm, "2026.2.3") { useInstaller = false }
        }
    }
}

// Exclude problematic coroutines debug dependencies
configurations.all {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug")
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug-jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    patchPluginXml {
        // 261.26222 = 2026.1.4, the first release with the LspIntegrationProvider API
        sinceBuild.set("261.26222")
        untilBuild.set("262.*")

        // Current-release notes; keep in sync with CHANGELOG.md when bumping the version
        changeNotes.set(
            """
            <h3>5.0.3</h3>
            <ul>
                <li>Available in all IntelliJ-based IDEs with the LSP API (2026.1.4+), no subscription required</li>
            </ul>
            <h3>5.0.0</h3>
            <ul>
                <li>Requires IDE version 2026.1.4 or newer (migrated to the new LspIntegrationProvider API)</li>
                <li>Build updated to Gradle 9.8, Kotlin 2.4 and Java 21</li>
            </ul>
            """.trimIndent(),
        )
    }

    test {
        // Disabled: the IntelliJ Platform plugin wires the platform test framework (and its
        // coroutines-debug agent) into the default task, which crashes plain JUnit runs.
        // Pure unit tests run via the custom `unitTest` task below.
        enabled = false
    }

    // Create a pure unit test task that doesn't use IntelliJ Platform
    register<Test>("unitTest") {
        description = "Run pure unit tests without IntelliJ Platform"
        group = "verification"

        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        // Use only test source files and minimal dependencies
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = configurations.testRuntimeClasspath.get().filter {
            !it.path.contains("idea") &&
                !it.path.contains("intellij") &&
                !it.path.contains("kotlinx-coroutines-debug")
        } + sourceSets.main.get().output + sourceSets.test.get().output

        // Standard JVM without IntelliJ Platform interference
        jvmArgs("-Xmx512m", "-XX:+UseG1GC")

        // Disable coroutines debug
        systemProperty("kotlinx.coroutines.debug", "off")
    }

    buildPlugin {
        archiveFileName.set("bkmr-lsp-plugin-${project.version}.zip")
    }

    runIde {
        jvmArgs = listOf("-Xmx2048m")
    }

    verifyPlugin {
        // Keep the verifier cache inside the workspace; ~/.pluginVerifier is not writable
        // in sandboxed environments and pollutes $HOME elsewhere
        systemProperty(
            "plugin.verifier.home.dir",
            layout.buildDirectory.dir("pluginVerifier").get().asFile.absolutePath,
        )
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("JETBRAINS_MARKETPLACE_TOKEN"))
    }
}
