package com.steeplesoft.roqidea.actions

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.steeplesoft.roqidea.services.RoqProjectDetector
import java.io.File

/**
 * Action to build the Roq site for production.
 */
class BuildRoqSiteAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val detector = project.service<RoqProjectDetector>()

        if (!detector.isRoqProject()) {
            Messages.showWarningDialog(
                project,
                "This is not a Roq project.",
                "Not a Roq Project"
            )
            return
        }

        val projectPath = project.basePath ?: return

        try {
            val isMaven = hasMaven(project)
            val isGradle = hasGradle(project)

            when {
                isMaven -> buildWithMaven(project, projectPath)
                isGradle -> buildWithGradle(project, projectPath)
                else -> {
                    Messages.showErrorDialog(
                        project,
                        "No Maven or Gradle build file found.",
                        "Build System Not Found"
                    )
                }
            }
        } catch (ex: Exception) {
            thisLogger().error("Failed to build Roq site", ex)
            Messages.showErrorDialog(
                project,
                "Failed to build site: ${ex.message}",
                "Build Error"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabled = false
            return
        }

        val detector = project.service<RoqProjectDetector>()
        e.presentation.isEnabled = detector.isRoqProject()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private fun buildWithMaven(project: Project, projectPath: String) {
        val mvnw = File(projectPath, if (isWindows()) "mvnw.cmd" else "mvnw")
        val command = if (mvnw.exists()) mvnw.absolutePath else "mvn"

        executeBuildCommand(
            project,
            projectPath,
            command,
            listOf(
                "clean",
                "package",
                "-DskipTests",
                "-Dquarkus.roq.generator.batch=true"
            ),
            "Maven Build"
        )
    }

    private fun buildWithGradle(project: Project, projectPath: String) {
        val gradlew = File(projectPath, if (isWindows()) "gradlew.bat" else "gradlew")
        val command = if (gradlew.exists()) gradlew.absolutePath else "gradle"

        executeBuildCommand(
            project,
            projectPath,
            command,
            listOf(
                "clean",
                "build",
                "-Dquarkus.roq.generator.batch=true"
            ),
            "Gradle Build"
        )
    }

    private fun executeBuildCommand(
        project: Project,
        workingDir: String,
        command: String,
        args: List<String>,
        title: String
    ) {
        val consoleView = TextConsoleBuilderFactory.getInstance()
            .createBuilder(project)
            .console

        consoleView.print("Building Roq site...\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        consoleView.print("Command: $command ${args.joinToString(" ")}\n\n", ConsoleViewContentType.SYSTEM_OUTPUT)

        val processBuilder = ProcessBuilder(listOf(command) + args)
            .directory(File(workingDir))
            .redirectErrorStream(true)

        val process = processBuilder.start()

        // Read output
        Thread {
            process.inputStream.bufferedReader().use { reader ->
                reader.lines().forEach { line ->
                    consoleView.print("$line\n", ConsoleViewContentType.NORMAL_OUTPUT)
                }
            }
        }.start()

        // Wait for completion in background
        Thread {
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                consoleView.print(
                    "\n✓ Build completed successfully!\n",
                    ConsoleViewContentType.LOG_INFO_OUTPUT
                )
                consoleView.print(
                    "Static site generated in: target/roq/ or build/roq/\n",
                    ConsoleViewContentType.SYSTEM_OUTPUT
                )
            } else {
                consoleView.print(
                    "\n✗ Build failed with exit code: $exitCode\n",
                    ConsoleViewContentType.ERROR_OUTPUT
                )
            }
        }.start()

        // Show console
        val descriptor = RunContentDescriptor(
            consoleView,
            null,
            consoleView.component,
            title
        )

        RunContentManager.getInstance(project).showRunContent(
            DefaultRunExecutor.getRunExecutorInstance(),
            descriptor
        )
    }

    private fun hasMaven(project: Project): Boolean {
        val pomFiles = FilenameIndex.getVirtualFilesByName(
            "pom.xml",
            GlobalSearchScope.projectScope(project)
        )
        return pomFiles.isNotEmpty()
    }

    private fun hasGradle(project: Project): Boolean {
        val buildFiles = listOf("build.gradle", "build.gradle.kts")
        return buildFiles.any { filename ->
            FilenameIndex.getVirtualFilesByName(
                filename,
                GlobalSearchScope.projectScope(project)
            ).isNotEmpty()
        }
    }

    private fun isWindows(): Boolean {
        return System.getProperty("os.name").lowercase().contains("win")
    }
}
