// File: src/main/kotlin/com/sysid/bkmr/BkmrConfigurable.kt
package com.sysid.bkmr

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

class BkmrConfigurable : Configurable {

    private lateinit var enableLspCheckBox: JBCheckBox
    private lateinit var bkmrLspBinaryField: TextFieldWithBrowseButton
    private lateinit var enableDebugLoggingCheckBox: JBCheckBox

    override fun getDisplayName(): String = "bkmr"

    override fun createComponent(): JComponent {
        val settings = BkmrSettings.getInstance()

        enableLspCheckBox = JBCheckBox("Enable LSP Integration", settings.enableLspIntegration)
        enableDebugLoggingCheckBox = JBCheckBox("Enable Debug Logging", settings.enableDebugLogging)

        bkmrLspBinaryField = TextFieldWithBrowseButton().apply {
            text = settings.bkmrLspBinaryPath
            addBrowseFolderListener(
                "Select bkmr-lsp Binary",
                "Choose the bkmr-lsp executable file",
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
            )
        }

        return panel {
            row("Enable LSP Integration:") {
                cell(enableLspCheckBox)
            }
            row("bkmr-lsp Binary Path:") {
                cell(bkmrLspBinaryField)
                    .comment("Path to the bkmr-lsp executable")
            }
            row("Enable Debug Logging:") {
                cell(enableDebugLoggingCheckBox)
                    .comment("Enable verbose logging for troubleshooting")
            }
        }
    }

    override fun isModified(): Boolean {
        val settings = BkmrSettings.getInstance()
        return enableLspCheckBox.isSelected != settings.enableLspIntegration ||
            bkmrLspBinaryField.text != settings.bkmrLspBinaryPath ||
            enableDebugLoggingCheckBox.isSelected != settings.enableDebugLogging
    }

    override fun apply() {
        val settings = BkmrSettings.getInstance()
        settings.enableLspIntegration = enableLspCheckBox.isSelected
        settings.bkmrLspBinaryPath = bkmrLspBinaryField.text
        settings.enableDebugLogging = enableDebugLoggingCheckBox.isSelected
    }

    override fun reset() {
        val settings = BkmrSettings.getInstance()
        enableLspCheckBox.isSelected = settings.enableLspIntegration
        bkmrLspBinaryField.text = settings.bkmrLspBinaryPath
        enableDebugLoggingCheckBox.isSelected = settings.enableDebugLogging
    }
}
