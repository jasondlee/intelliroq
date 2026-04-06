package com.steeplesoft.roqidea.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.steeplesoft.roqidea.seo.SeoAnalyzer
import com.steeplesoft.roqidea.services.RoqProjectDetector

/**
 * Action to analyze SEO for current content file.
 */
class AnalyzeSeoAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val detector = project.service<RoqProjectDetector>()
        if (!detector.isRoqProject()) {
            Messages.showWarningDialog(
                project,
                "This is not a Roq project.",
                "Not a Roq Project"
            )
            return
        }

        val analyzer = project.service<SeoAnalyzer>()
        val issues = analyzer.analyzeFile(file)

        if (issues.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "No SEO issues found!\n\nThis content file looks good for SEO.",
                "SEO Analysis - ${file.name}"
            )
            return
        }

        val report = buildString {
            appendLine("SEO Analysis Results for: ${file.name}")
            appendLine()

            val errors = issues.filter { it.severity == SeoAnalyzer.Severity.ERROR }
            val warnings = issues.filter { it.severity == SeoAnalyzer.Severity.WARNING }
            val info = issues.filter { it.severity == SeoAnalyzer.Severity.INFO }

            if (errors.isNotEmpty()) {
                appendLine("ERRORS (${errors.size}):")
                errors.forEach { appendLine("  ❌ ${it.message}") }
                appendLine()
            }

            if (warnings.isNotEmpty()) {
                appendLine("WARNINGS (${warnings.size}):")
                warnings.forEach { appendLine("  ⚠️  ${it.message}") }
                appendLine()
            }

            if (info.isNotEmpty()) {
                appendLine("SUGGESTIONS (${info.size}):")
                info.forEach { appendLine("  ℹ️  ${it.message}") }
            }
        }

        Messages.showMessageDialog(
            project,
            report,
            "SEO Analysis",
            Messages.getInformationIcon()
        )
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)

        // Enable only for content files (markdown in content directory)
        e.presentation.isEnabledAndVisible = project != null &&
                file != null &&
                file.path.contains("/content/") &&
                (file.extension == "md" || file.extension == "html")
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
