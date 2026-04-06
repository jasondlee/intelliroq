package com.steeplesoft.roqidea.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

/**
 * Run configuration for Roq dev mode.
 */
class RoqDevModeRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : LocatableConfigurationBase<RoqDevModeRunConfigurationOptions>(project, factory, name) {

    private val myOptions: RoqDevModeRunConfigurationOptions
        get() = super.getOptions() as RoqDevModeRunConfigurationOptions

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return RoqDevModeSettingsEditor()
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return RoqDevModeRunState(environment, this)
    }

    fun getPort(): Int = myOptions.port

    fun setPort(port: Int) {
        myOptions.port = port
    }

    fun getEnvironmentVariables(): Map<String, String> = myOptions.environmentVariables

    fun setEnvironmentVariables(envVars: Map<String, String>) {
        myOptions.environmentVariables = envVars.toMutableMap()
    }
}
