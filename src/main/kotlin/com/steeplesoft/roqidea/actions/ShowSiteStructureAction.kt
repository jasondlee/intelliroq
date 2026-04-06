package com.steeplesoft.roqidea.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.Messages
import com.steeplesoft.roqidea.services.RoqProjectDetector
import com.steeplesoft.roqidea.structure.SiteStructureAnalyzer
import javax.swing.JScrollPane
import javax.swing.JTextArea

/**
 * Action to show site structure visualization.
 */
class ShowSiteStructureAction : AnAction() {

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

        val analyzer = project.service<SiteStructureAnalyzer>()
        val report = analyzer.generateStructureReport()

        val textArea = JTextArea(report).apply {
            isEditable = false
            font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)
        }

        val scrollPane = JScrollPane(textArea)
        scrollPane.preferredSize = java.awt.Dimension(500, 400)

        val dialog = DialogBuilder(project)
        dialog.setTitle("Site Structure")
        dialog.setCenterPanel(scrollPane)
        dialog.removeAllActions()
        dialog.addOkAction().setText("Close")
        dialog.show()
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
