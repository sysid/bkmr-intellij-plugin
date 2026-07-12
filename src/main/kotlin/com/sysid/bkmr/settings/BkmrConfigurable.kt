// File: src/main/kotlin/com/sysid/bkmr/settings/BkmrConfigurable.kt
package com.sysid.bkmr.settings

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.panel
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
            val descriptor = FileChooserDescriptor(true, false, false, false, false, false).apply {
                title = "Select bkmr Binary"
                description = "Choose the bkmr executable file"
            }
            addActionListener {
                FileChooser.chooseFile(descriptor, null, null) { file ->
                    text = file.path
                }
            }
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
                text(
                    """
                    <b>Usage:</b><br/>
                    Snippets appear automatically in the completion popup while typing.<br/>
                    Use Ctrl+Space for manual completion or Tab/Shift+Tab to navigate snippet placeholders.<br/>
                    Requires the bkmr command-line tool (v4.24.0+) in PATH or configured above.
                    """.trimIndent(),
                )
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
