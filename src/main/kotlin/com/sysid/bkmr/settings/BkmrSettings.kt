// File: src/main/kotlin/com/sysid/bkmr/settings/BkmrSettings.kt
package com.sysid.bkmr

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.util.SystemInfo

@State(
    name = "BkmrSettings",
    storages = [Storage("bkmr.xml")]
)
@Service(Service.Level.APP)
class BkmrSettings : PersistentStateComponent<BkmrSettings> {

    var enableLspIntegration: Boolean = true
    var bkmrBinaryPath: String = findDefaultBinaryPath()
    var enableDebugLogging: Boolean = false

    companion object {
        fun getInstance(): BkmrSettings =
            ApplicationManager.getApplication().getService(BkmrSettings::class.java)

        private fun findDefaultBinaryPath(): String = when {
            SystemInfo.isWindows -> "bkmr.exe"
            else -> "bkmr"
        }
    }

    override fun getState(): BkmrSettings = this

    override fun loadState(state: BkmrSettings) {
        enableLspIntegration = state.enableLspIntegration
        bkmrBinaryPath = state.bkmrBinaryPath
        enableDebugLogging = state.enableDebugLogging
    }
}
