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
        // Only start for text files (you can customize this)
        if (file.isDirectory || file.extension?.lowercase() in listOf("jpg", "png", "gif", "pdf", "zip")) {
            return
        }

        // Get settings (will create this next)
        val settings = BkmrSettings.getInstance()
        if (!settings.enableLspIntegration || settings.bkmrLspBinaryPath.isBlank()) {
            return
        }

        serverStarter.ensureServerStarted(BkmrLspServerDescriptor(project))
    }
}

class BkmrLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "bkmr-lsp") {

    override fun isSupportedFile(file: VirtualFile): Boolean {
        // Support all text files except binaries
        return !file.isDirectory &&
               file.extension?.lowercase() !in listOf("jpg", "png", "gif", "pdf", "zip", "exe", "dll", "so")
    }

    override fun createCommandLine(): GeneralCommandLine {
        val settings = BkmrSettings.getInstance()

        return GeneralCommandLine().apply {
            exePath = settings.bkmrLspBinaryPath
            withWorkDirectory(project.basePath)
            // Add any environment variables your LSP server needs
            withEnvironment("RUST_LOG", if (settings.enableDebugLogging) "debug" else "info")
        }
    }
}

