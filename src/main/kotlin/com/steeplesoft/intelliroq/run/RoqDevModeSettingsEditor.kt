package com.steeplesoft.intelliroq.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Settings editor for Roq dev mode run configuration.
 */
class RoqDevModeSettingsEditor : SettingsEditor<RoqDevModeRunConfiguration>() {
    private val portField = JBTextField()
    private val panel: JPanel

    init {
        portField.text = "8080"

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Port:"), portField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun resetEditorFrom(configuration: RoqDevModeRunConfiguration) {
        portField.text = configuration.getPort().toString()
    }

    override fun applyEditorTo(configuration: RoqDevModeRunConfiguration) {
        try {
            configuration.setPort(portField.text.toInt())
        } catch (e: NumberFormatException) {
            configuration.setPort(8080)
        }
    }

    override fun createEditor(): JComponent = panel
}
