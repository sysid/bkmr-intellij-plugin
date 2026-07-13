import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    id("org.jetbrains.intellij.platform") version "2.18.1"
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
        intellijIdeaUltimate("2025.2", useInstaller = false)
        pluginVerifier()
    }

    // Unit test dependencies (no platform dependencies)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

ktlint {
    // Pin the ktlint engine so local runs and CI enforce identical rules
    version.set("1.8.0")
}

intellijPlatform {
    pluginVerification {
        ides {
            // Explicit IU versions (sinceBuild floor + current target): letting the matrix pick
            // Community releases would false-fail on the com.intellij.modules.ultimate dependency.
            // useInstaller = false for the same reason as the main dependency (ZIP, no hdiutil).
            ide(IntelliJPlatformType.IntellijIdeaUltimate, "2024.2", useInstaller = false)
            ide(IntelliJPlatformType.IntellijIdeaUltimate, "2025.2", useInstaller = false)
        }
    }
}

// Exclude problematic coroutines debug dependencies
configurations.all {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug")
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug-jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("262.*")

        // Current-release notes; keep in sync with CHANGELOG.md when bumping the version
        changeNotes.set(
            """
            <h3>3.0.0</h3>
            <ul>
                <li>Use the consolidated `bkmr lsp` command (bkmr-lsp binary no longer needed)</li>
                <li>Notification when the configured bkmr binary cannot be found</li>
                <li>Filepath comment: shebang and BOM aware, no duplicate insertion</li>
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
