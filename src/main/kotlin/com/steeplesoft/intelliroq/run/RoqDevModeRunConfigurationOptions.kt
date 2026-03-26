package com.steeplesoft.intelliroq.run

import com.intellij.execution.configurations.RunConfigurationOptions

/**
 * Options for Roq dev mode run configuration.
 */
class RoqDevModeRunConfigurationOptions : RunConfigurationOptions() {
    private val portProperty = property(8080)
    private val envVarsProperty = map<String, String>()

    var port: Int
        get() = portProperty.getValue(this)
        set(value) = portProperty.setValue(this, value)

    var environmentVariables: MutableMap<String, String>
        get() = envVarsProperty.getValue(this) ?: mutableMapOf()
        set(value) = envVarsProperty.setValue(this, value)
}
