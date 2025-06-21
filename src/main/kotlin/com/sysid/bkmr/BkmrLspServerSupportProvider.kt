// File: src/main/kotlin/com/sysid/bkmr/BkmrLspServerSupportProvider.kt
package com.sysid.bkmr

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor

class BkmrLspServerSupportProvider : LspServerSupportProvider {

    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        // Skip directories and binary files
        if (file.isDirectory) {
            return
        }

        val settings = BkmrSettings.getInstance()
        if (!settings.enableLspIntegration || settings.bkmrLspBinaryPath.isBlank()) {
            return
        }

        // Debug logging for scratch files
        if (settings.enableDebugLogging) {
            println("BKMR LSP: File opened - ${file.path}")
            println("BKMR LSP: File name - ${file.name}")
            println("BKMR LSP: File extension - ${file.extension}")
            println("BKMR LSP: Project base path - ${project.basePath}")
            println("BKMR LSP: Is supported - ${isSupportedFile(file)}")
        }

        // Start LSP server for supported files
        if (isSupportedFile(file)) {
            serverStarter.ensureServerStarted(BkmrLspServerDescriptor(project))
        }
    }

    private fun isSupportedFile(file: VirtualFile): Boolean {
        // Support all text files, exclude only known binary types
        val extension = file.extension?.lowercase()

        // Exclude known binary file types
        val binaryExtensions = setOf(
            "jpg", "jpeg", "png", "gif", "bmp", "ico", "svg",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "zip", "tar", "gz", "rar", "7z", "bz2",
            "exe", "dll", "so", "dylib", "app", "dmg",
            "mp3", "mp4", "avi", "mov", "wav", "flac",
            "class", "jar", "war", "ear"
        )

        return !file.isDirectory && extension !in binaryExtensions
    }
}

class BkmrLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "bkmr-lsp") {

    override fun isSupportedFile(file: VirtualFile): Boolean {
        // Support all text files, exclude only known binary types
        val extension = file.extension?.lowercase()

        // Exclude known binary file types
        val binaryExtensions = setOf(
            "jpg", "jpeg", "png", "gif", "bmp", "ico", "svg",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "zip", "tar", "gz", "rar", "7z", "bz2",
            "exe", "dll", "so", "dylib", "app", "dmg",
            "mp3", "mp4", "avi", "mov", "wav", "flac",
            "class", "jar", "war", "ear"
        )

        return !file.isDirectory && extension !in binaryExtensions
    }

    override fun createCommandLine(): GeneralCommandLine {
        val settings = BkmrSettings.getInstance()

        return GeneralCommandLine().apply {
            exePath = settings.bkmrLspBinaryPath
            withWorkDirectory(project.basePath)
            withEnvironment("RUST_LOG", if (settings.enableDebugLogging) "debug" else "info")
        }
    }

    override fun createInitializationOptions(): Any? {
        // Provide initialization options to ensure proper setup
        return mapOf(
            "bkmr" to mapOf(
                "maxCompletions" to 50,
                "triggerCharacters" to listOf(":")
            )
        )
    }
}
