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
        intellijIdeaUltimate("2025.1.1.1")
    }
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
        untilBuild.set("251.*")

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
        useJUnit()
        testLogging {
            events("passed", "skipped", "failed")
        }
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
