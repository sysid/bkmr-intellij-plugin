// File: src/main/kotlin/com/sysid/bkmr/settings/BkmrConfigurable.kt
package com.sysid.bkmr

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

class BkmrConfigurable : Configurable {

    private lateinit var enableLspCheckBox: JBCheckBox
    private lateinit var bkmrBinaryField: TextFieldWithBrowseButton
    private lateinit var enableDebugLoggingCheckBox: JBCheckBox

    override fun getDisplayName(): String = "bkmr"

    override fun createComponent(): JComponent {
        val settings = BkmrSettings.getInstance()

        enableLspCheckBox = JBCheckBox("Enable LSP Integration", settings.enableLspIntegration)
        enableDebugLoggingCheckBox = JBCheckBox("Enable Debug Logging", settings.enableDebugLogging)

        bkmrBinaryField = TextFieldWithBrowseButton().apply {
            text = settings.bkmrBinaryPath
            addBrowseFolderListener(
                "Select bkmr Binary",
                "Choose the bkmr executable file",
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
            )
        }

        return panel {
            row("Enable LSP Integration:") {
                cell(enableLspCheckBox)
            }
            row("bkmr Binary Path:") {
                cell(bkmrBinaryField)
                    .comment("Path to the bkmr executable")
            }
            row("Enable Debug Logging:") {
                cell(enableDebugLoggingCheckBox)
                    .comment("Enable verbose logging for troubleshooting")
            }
            row {
                text("""
                    <b>Usage:</b><br/>
                    Snippets appear automatically in completion popup while typing.<br/>
                    Use Ctrl+Space for manual completion or Tab/Shift+Tab to navigate snippet placeholders.<br/>
                    Language-specific filtering and universal snippets are supported.
                """.trimIndent())
            }
        }
    }

    override fun isModified(): Boolean {
        val settings = BkmrSettings.getInstance()
        return enableLspCheckBox.isSelected != settings.enableLspIntegration ||
            bkmrBinaryField.text != settings.bkmrBinaryPath ||
            enableDebugLoggingCheckBox.isSelected != settings.enableDebugLogging
    }

    override fun apply() {
        val settings = BkmrSettings.getInstance()
        settings.enableLspIntegration = enableLspCheckBox.isSelected
        settings.bkmrBinaryPath = bkmrBinaryField.text
        settings.enableDebugLogging = enableDebugLoggingCheckBox.isSelected
    }

    override fun reset() {
        val settings = BkmrSettings.getInstance()
        enableLspCheckBox.isSelected = settings.enableLspIntegration
        bkmrBinaryField.text = settings.bkmrBinaryPath
        enableDebugLoggingCheckBox.isSelected = settings.enableDebugLogging
    }
}
