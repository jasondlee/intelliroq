package com.steeplesoft.intelliroq.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.steeplesoft.intelliroq.services.RoqPluginManager
import com.steeplesoft.intelliroq.services.RoqProjectDetector

/**
 * Action to view installed Roq plugins.
 */
class ViewInstalledPluginsAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val detector = project.service<RoqProjectDetector>()

        // Ensure it's a Roq project
        if (!detector.isRoqProject()) {
            Messages.showWarningDialog(
                project,
                "This is not a Roq project.",
                "Not a Roq Project"
            )
            return
        }

        val pluginManager = project.service<RoqPluginManager>()
        val installedPlugins = pluginManager.getInstalledPlugins()
        val roqVersion = pluginManager.getRoqVersion()

        if (installedPlugins.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "No Roq plugins are currently installed in this project.\n\n" +
                "Roq Version: ${roqVersion ?: "Unknown"}\n\n" +
                "Use 'Add Roq Plugin...' to install plugins.",
                "Installed Roq Plugins"
            )
            return
        }

        val pluginList = installedPlugins.joinToString("\n") { plugin ->
            "• ${plugin.plugin.displayName} (${plugin.plugin.artifactId})" +
            (plugin.version?.let { " - v$it" } ?: "") +
            "\n  ${plugin.plugin.description}"
        }

        val message = "Roq Version: ${roqVersion ?: "Unknown"}\n\n" +
                     "Installed Plugins (${installedPlugins.size}):\n\n" +
                     pluginList

        Messages.showInfoMessage(
            project,
            message,
            "Installed Roq Plugins"
        )
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
}
