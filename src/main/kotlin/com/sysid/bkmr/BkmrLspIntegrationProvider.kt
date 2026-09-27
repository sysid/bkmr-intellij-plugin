// File: src/main/kotlin/com/sysid/bkmr/BkmrLspIntegrationProvider.kt
package com.sysid.bkmr

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.api.ProjectWideLspClientDescriptor
import com.sysid.bkmr.settings.BkmrSettings
import java.io.File

private const val MAX_COMPLETIONS = 50

class BkmrLspIntegrationProvider : LspIntegrationProvider {

    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        clientStarter: LspIntegrationProvider.LspClientStarter,
    ) {
        if (file.isDirectory) {
            return
        }

        val settings = BkmrSettings.getInstance()
        if (!settings.enableLspIntegration || settings.bkmrBinaryPath.isBlank()) {
            return
        }

        val supported = FileSupport.isSupportedExtension(file.extension)
        if (settings.enableDebugLogging) {
            LOG.info(
                "bkmr LSP: file=${file.path} extension=${file.extension} " +
                    "basePath=${project.basePath} supported=$supported",
            )
        }

        if (!supported) {
            return
        }

        if (resolveBinary(settings.bkmrBinaryPath) == null) {
            notifyMissingBinaryOnce(project, settings.bkmrBinaryPath)
            return
        }

        clientStarter.ensureClientStarted(BkmrLspClientDescriptor(project))
    }

    companion object {
        private val LOG = logger<BkmrLspIntegrationProvider>()

        // Notify once per IDE session, not once per opened file
        @Volatile
        private var missingBinaryNotified = false

        /** Resolves the configured binary: absolute path must be executable, bare name is looked up in PATH. */
        internal fun resolveBinary(configuredPath: String): File? {
            val candidate = File(configuredPath)
            if (candidate.isAbsolute) {
                return candidate.takeIf { it.isFile && it.canExecute() }
            }
            return PathEnvironmentVariableUtil.findInPath(configuredPath)
        }

        private fun notifyMissingBinaryOnce(project: Project, configuredPath: String) {
            if (missingBinaryNotified) {
                return
            }
            missingBinaryNotified = true
            LOG.warn("bkmr binary not found: '$configuredPath' — LSP server not started")
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Bkmr Notifications")
                .createNotification(
                    "bkmr binary not found",
                    "'$configuredPath' does not resolve to an executable. " +
                        "Configure the path in Settings → Tools → bkmr.",
                    NotificationType.WARNING,
                )
                .notify(project)
        }
    }
}

class BkmrLspClientDescriptor(project: Project) : ProjectWideLspClientDescriptor(project, "bkmr") {

    override fun isSupportedFile(file: VirtualFile): Boolean =
        !file.isDirectory && FileSupport.isSupportedExtension(file.extension)

    override fun createCommandLine(): GeneralCommandLine {
        val settings = BkmrSettings.getInstance()

        return GeneralCommandLine().apply {
            exePath = settings.bkmrBinaryPath
            addParameter("lsp")
            withWorkDirectory(project.basePath)
            withEnvironment("RUST_LOG", if (settings.enableDebugLogging) "debug" else "info")
        }
    }

    override fun createInitializationOptions(): Any? = mapOf(
        "bkmr" to mapOf(
            "maxCompletions" to MAX_COMPLETIONS,
        ),
    )
}
