package com.steeplesoft.roqidea.run

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import java.io.File

/**
 * Run state for executing Roq dev mode.
 */
class RoqDevModeRunState(
    environment: ExecutionEnvironment,
    private val configuration: RoqDevModeRunConfiguration
) : CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        val commandLine = createCommandLine()
        val processHandler = KillableColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }

    private fun createCommandLine(): GeneralCommandLine {
        val project = configuration.project
        val projectPath = project.basePath ?: throw IllegalStateException("Project path not found")

        // Determine build system
        val isMaven = hasMaven(project)
        val isGradle = hasGradle(project)

        val commandLine = GeneralCommandLine()
        commandLine.withWorkDirectory(projectPath)
        commandLine.withEnvironment(configuration.getEnvironmentVariables())

        when {
            isMaven -> {
                // Use Maven wrapper if available, otherwise mvn
                val mvnw = File(projectPath, if (isWindows()) "mvnw.cmd" else "mvnw")
                if (mvnw.exists()) {
                    commandLine.exePath = mvnw.absolutePath
                } else {
                    commandLine.exePath = "mvn"
                }
                commandLine.addParameter("quarkus:dev")
                commandLine.addParameter("-Dquarkus.http.port=${configuration.getPort()}")
            }
            isGradle -> {
                // Use Gradle wrapper if available, otherwise gradle
                val gradlew = File(projectPath, if (isWindows()) "gradlew.bat" else "gradlew")
                if (gradlew.exists()) {
                    commandLine.exePath = gradlew.absolutePath
                } else {
                    commandLine.exePath = "gradle"
                }
                commandLine.addParameter("quarkusDev")
                commandLine.addParameter("--console=plain")
                commandLine.addParameter("-Dquarkus.http.port=${configuration.getPort()}")
            }
            else -> {
                throw IllegalStateException("No Maven or Gradle build file found")
            }
        }

        return commandLine
    }

    private fun hasMaven(project: com.intellij.openapi.project.Project): Boolean {
        val pomFiles = FilenameIndex.getFilesByName(
            project,
            "pom.xml",
            GlobalSearchScope.projectScope(project)
        )
        return pomFiles.isNotEmpty()
    }

    private fun hasGradle(project: com.intellij.openapi.project.Project): Boolean {
        val buildFiles = listOf("build.gradle", "build.gradle.kts")
        return buildFiles.any { filename ->
            FilenameIndex.getFilesByName(
                project,
                filename,
                GlobalSearchScope.projectScope(project)
            ).isNotEmpty()
        }
    }

    private fun isWindows(): Boolean {
        return System.getProperty("os.name").lowercase().contains("win")
    }
}
