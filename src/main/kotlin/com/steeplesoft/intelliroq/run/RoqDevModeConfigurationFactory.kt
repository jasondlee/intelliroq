package com.steeplesoft.intelliroq.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

/**
 * Factory for creating Roq dev mode run configurations.
 */
class RoqDevModeConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String = "Roq Dev Mode"

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return RoqDevModeRunConfiguration(project, this, "Roq Dev Mode")
    }

    override fun getOptionsClass(): Class<out BaseState> {
        return RoqDevModeRunConfigurationOptions::class.java
    }
}
