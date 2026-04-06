package com.steeplesoft.roqidea.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.steeplesoft.roqidea.services.RoqProjectDetector
import java.io.IOException

/**
 * Action to initialize a Roq project structure in the current project.
 */
class InitializeRoqProjectAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val detector = project.service<RoqProjectDetector>()

        // Check if already a Roq project
        if (detector.isRoqProject()) {
            Messages.showInfoMessage(
                project,
                "This project already appears to be a Roq project.",
                "Roq Project Already Initialized"
            )
            return
        }

        // Confirm with user
        val result = Messages.showYesNoDialog(
            project,
            "This will create the Roq project structure (content/, templates/, data/, etc.) in your project root.\n\n" +
            "Do you want to continue?",
            "Initialize Roq Project",
            Messages.getQuestionIcon()
        )

        if (result != Messages.YES) {
            return
        }

        try {
            initializeRoqStructure(project)
            Messages.showInfoMessage(
                project,
                "Roq project structure has been created successfully!\n\n" +
                "Next steps:\n" +
                "1. Add Roq dependencies to your build file\n" +
                "2. Configure application.properties\n" +
                "3. Start creating content in the content/ directory",
                "Roq Project Initialized"
            )
        } catch (ex: Exception) {
            thisLogger().error("Failed to initialize Roq project", ex)
            Messages.showErrorDialog(
                project,
                "Failed to initialize Roq project: ${ex.message}",
                "Initialization Error"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    /**
     * Creates the Roq project structure and initial files.
     */
    private fun initializeRoqStructure(project: Project) {
        val baseDir = project.baseDir ?: throw IOException("Project base directory not found")

        // Ask user for site URL
        val siteUrl = Messages.showInputDialog(
            project,
            "Enter the site URL (e.g., https://example.com):",
            "Site URL Configuration",
            Messages.getQuestionIcon(),
            "http://localhost:8080",
            null
        ) ?: return

        WriteCommandAction.runWriteCommandAction(project) {
            // Create directory structure
            com.steeplesoft.roqidea.wizard.RoqProjectInitializer.createDirectoryStructure(baseDir)

            // Create sample files
            com.steeplesoft.roqidea.wizard.RoqProjectInitializer.createSampleFiles(baseDir, siteUrl)

            // Create or update application.properties
            com.steeplesoft.roqidea.wizard.RoqProjectInitializer.createApplicationProperties(baseDir, siteUrl)

            // Refresh the file system
            baseDir.refresh(false, true)
        }
    }
}
