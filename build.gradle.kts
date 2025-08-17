plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    id("org.jetbrains.intellij.platform") version "2.6.0"
}

group = "com.sysid"
version = "1.1.0"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    intellijPlatform {
        intellijIdeaUltimate("2025.2")
    }
    
    // Unit test dependencies (no platform dependencies)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
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

        changeNotes.set(
            """
            <ul>
                <li>Initial release of bkmr-lsp integration</li>
                <li>Code completion for bkmr snippets</li>
                <li>Execute snippet actions</li>
                <li>Search and filter snippets</li>
            </ul>
        """.trimIndent()
        )
    }

    test {
        // Disable default test task since IntelliJ Platform plugin interferes
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

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("JETBRAINS_MARKETPLACE_TOKEN"))
    }

    register("printToken") {
        description = "Print the JetBrains Marketplace token status"
        group = "publishing"
        doLast {
            println("Token: " + (project.findProperty("token") ?: "NOT SET"))
        }
    }
}
