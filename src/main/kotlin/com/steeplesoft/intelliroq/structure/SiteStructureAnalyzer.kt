package com.steeplesoft.intelliroq.structure

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.steeplesoft.intelliroq.services.RoqProjectDetector

/**
 * Service for analyzing site structure.
 */
@Service(Service.Level.PROJECT)
class SiteStructureAnalyzer(private val project: Project) {

    data class PageNode(
        val file: VirtualFile,
        val title: String,
        val path: String,
        val children: MutableList<PageNode> = mutableListOf()
    )

    /**
     * Analyzes the site structure and returns a tree of pages.
     */
    fun analyzeSiteStructure(): PageNode? {
        val detector = project.service<RoqProjectDetector>()
        val dirs = detector.getRoqDirectories()

        val contentDir = dirs.contentDir ?: return null

        return buildPageTree(contentDir, "")
    }

    private fun buildPageTree(directory: VirtualFile, parentPath: String): PageNode {
        val root = PageNode(
            file = directory,
            title = if (directory.name == "content") "Site Root" else directory.name,
            path = parentPath
        )

        directory.children.forEach { child ->
            if (child.isDirectory) {
                val subTree = buildPageTree(child, "$parentPath/${child.name}")
                root.children.add(subTree)
            } else if (child.extension in listOf("md", "html", "adoc")) {
                val title = extractTitle(child)
                root.children.add(PageNode(
                    file = child,
                    title = title,
                    path = "$parentPath/${child.nameWithoutExtension}"
                ))
            }
        }

        return root
    }

    private fun extractTitle(file: VirtualFile): String {
        try {
            val content = String(file.contentsToByteArray())

            // Try to extract from frontmatter
            if (content.trimStart().startsWith("---")) {
                val lines = content.lines()
                for (line in lines.drop(1)) {
                    if (line.startsWith("---")) break
                    if (line.startsWith("title:")) {
                        return line.substringAfter("title:").trim().removeSurrounding("\"")
                    }
                }
            }

            // Try to find first H1
            val h1Match = Regex("^#\\s+(.+)$", RegexOption.MULTILINE).find(content)
            if (h1Match != null) {
                return h1Match.groupValues[1]
            }

            // Fallback to filename
            return file.nameWithoutExtension.replace("-", " ").replace("_", " ")
                .split(" ").joinToString(" ") { it.capitalize() }

        } catch (e: Exception) {
            return file.nameWithoutExtension
        }
    }

    /**
     * Generates a text representation of the site structure.
     */
    fun generateStructureReport(): String {
        val root = analyzeSiteStructure() ?: return "No content directory found"

        val builder = StringBuilder()
        builder.appendLine("Site Structure")
        builder.appendLine("==============")
        builder.appendLine()

        appendNodeToReport(root, builder, 0)

        return builder.toString()
    }

    private fun appendNodeToReport(node: PageNode, builder: StringBuilder, level: Int) {
        val indent = "  ".repeat(level)
        val icon = if (node.file.isDirectory) "📁" else "📄"

        builder.appendLine("$indent$icon ${node.title}")

        node.children.sortedBy { it.title }.forEach { child ->
            appendNodeToReport(child, builder, level + 1)
        }
    }
}
