package com.steeplesoft.roqidea.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.icons.AllIcons
import javax.swing.Icon

/**
 * Run configuration type for Roq dev mode.
 */
class RoqDevModeConfigurationType : ConfigurationType {
    override fun getDisplayName(): String = "Roq Dev Mode"

    override fun getConfigurationTypeDescription(): String =
        "Run Roq static site in Quarkus development mode"

    override fun getIcon(): Icon = AllIcons.Nodes.Console

    override fun getId(): String = "RoqDevModeConfiguration"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(RoqDevModeConfigurationFactory(this))
}
