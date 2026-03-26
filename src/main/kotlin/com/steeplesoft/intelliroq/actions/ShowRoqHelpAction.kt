package com.steeplesoft.intelliroq.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import javax.swing.JEditorPane
import javax.swing.JScrollPane

/**
 * Action to show Roq help and documentation.
 */
class ShowRoqHelpAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val helpText = """
            <html>
            <body style='font-family: sans-serif; padding: 10px;'>
            <h2>IntelliRoq Plugin Help</h2>

            <h3>Getting Started</h3>
            <ol>
                <li>Create or open a Java/Maven/Gradle project</li>
                <li>Use <b>Tools > Roq > Initialize Roq Project Structure</b></li>
                <li>Add Roq dependencies to your build file</li>
                <li>Start creating content!</li>
            </ol>

            <h3>Features</h3>
            <ul>
                <li><b>Project Detection</b> - Automatically detects Roq projects</li>
                <li><b>Content Creation</b> - Wizards for data files, layouts, partials</li>
                <li><b>Plugin Management</b> - Add and view Roq plugins</li>
                <li><b>Dev Mode</b> - Run site with live reload</li>
                <li><b>Production Build</b> - Generate static site</li>
                <li><b>Tool Window</b> - Quick overview and actions</li>
            </ul>

            <h3>Quick Actions</h3>
            <p>Access all features via <b>Tools > Roq</b> menu:</p>
            <ul>
                <li>Initialize Roq Project Structure</li>
                <li>New Data File / Layout / Partial</li>
                <li>Add Roq Plugin</li>
                <li>View Installed Plugins</li>
                <li>Build Static Site</li>
            </ul>

            <h3>Dev Mode</h3>
            <ol>
                <li>Go to <b>Run > Edit Configurations</b></li>
                <li>Click <b>+</b> and select <b>Roq Dev Mode</b></li>
                <li>Configure port (default: 8080)</li>
                <li>Run the configuration</li>
                <li>Site will be available at http://localhost:8080</li>
            </ol>

            <h3>Project Structure</h3>
            <ul>
                <li><b>content/</b> - Pages and blog posts (Markdown/HTML)</li>
                <li><b>templates/layouts/</b> - Page layout templates</li>
                <li><b>templates/partials/</b> - Reusable template components</li>
                <li><b>data/</b> - Structured data (YAML/JSON)</li>
                <li><b>static/</b> - Static assets (served with prefix)</li>
                <li><b>public/</b> - Public assets (served at root)</li>
            </ul>

            <h3>Roq Plugins</h3>
            <p>Available plugins:</p>
            <ul>
                <li><b>markdown</b> - CommonMark content (included by default)</li>
                <li><b>tagging</b> - Dynamic tag pages</li>
                <li><b>sitemap</b> - Auto-generate sitemap.xml</li>
                <li><b>lunr</b> - Client-side search</li>
                <li><b>asciidoc</b> - AsciiDoc support</li>
                <li>...and more</li>
            </ul>

            <h3>Resources</h3>
            <p><a href="https://iamroq.com">Roq Official Site</a></p>
            <p><a href="https://docs.quarkiverse.io/quarkus-roq/dev/index.html">Roq Documentation</a></p>
            <p><a href="https://github.com/jasondlee/intelliroq">IntelliRoq Plugin on GitHub</a></p>

            <h3>Support</h3>
            <p>Report issues: <a href="https://github.com/jasondlee/intelliroq/issues">GitHub Issues</a></p>
            </body>
            </html>
        """.trimIndent()

        val editorPane = JEditorPane("text/html", helpText).apply {
            isEditable = false
            addHyperlinkListener { event ->
                if (event.eventType == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                    BrowserUtil.browse(event.url)
                }
            }
        }

        val scrollPane = JScrollPane(editorPane)
        scrollPane.preferredSize = java.awt.Dimension(600, 500)

        val dialog = DialogBuilder(project)
        dialog.setTitle("Roq Plugin Help")
        dialog.setCenterPanel(scrollPane)
        dialog.removeAllActions()
        dialog.addOkAction().setText("Close")
        dialog.show()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
