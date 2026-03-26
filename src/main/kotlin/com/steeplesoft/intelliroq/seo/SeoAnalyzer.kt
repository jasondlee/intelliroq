package com.steeplesoft.intelliroq.seo

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException

/**
 * Service for analyzing SEO aspects of Roq content.
 */
@Service(Service.Level.PROJECT)
class SeoAnalyzer(private val project: Project) {

    data class SeoIssue(
        val file: VirtualFile,
        val severity: Severity,
        val message: String
    )

    enum class Severity {
        ERROR, WARNING, INFO
    }

    /**
     * Analyzes a content file for SEO issues.
     */
    fun analyzeFile(file: VirtualFile): List<SeoIssue> {
        val issues = mutableListOf<SeoIssue>()

        try {
            val content = String(file.contentsToByteArray())

            // Check for frontmatter
            if (!content.trimStart().startsWith("---")) {
                issues.add(SeoIssue(
                    file,
                    Severity.WARNING,
                    "Missing frontmatter - consider adding title and description"
                ))
                return issues
            }

            val frontmatter = extractFrontmatter(content)

            // Check for title
            if (!frontmatter.containsKey("title")) {
                issues.add(SeoIssue(
                    file,
                    Severity.ERROR,
                    "Missing title in frontmatter"
                ))
            } else {
                val title = frontmatter["title"] ?: ""
                if (title.length > 60) {
                    issues.add(SeoIssue(
                        file,
                        Severity.WARNING,
                        "Title too long (${title.length} chars) - recommended max 60"
                    ))
                }
                if (title.length < 10) {
                    issues.add(SeoIssue(
                        file,
                        Severity.WARNING,
                        "Title too short (${title.length} chars) - recommended min 10"
                    ))
                }
            }

            // Check for description
            if (!frontmatter.containsKey("description")) {
                issues.add(SeoIssue(
                    file,
                    Severity.WARNING,
                    "Missing description in frontmatter - important for SEO"
                ))
            } else {
                val desc = frontmatter["description"] ?: ""
                if (desc.length > 160) {
                    issues.add(SeoIssue(
                        file,
                        Severity.WARNING,
                        "Description too long (${desc.length} chars) - recommended max 160"
                    ))
                }
                if (desc.length < 50) {
                    issues.add(SeoIssue(
                        file,
                        Severity.INFO,
                        "Description short (${desc.length} chars) - recommended 120-160"
                    ))
                }
            }

            // Check for date
            if (!frontmatter.containsKey("date") && file.path.contains("/posts/")) {
                issues.add(SeoIssue(
                    file,
                    Severity.INFO,
                    "Missing date in blog post - consider adding publication date"
                ))
            }

            // Check for tags/keywords
            if (!frontmatter.containsKey("tags") && !frontmatter.containsKey("keywords")) {
                issues.add(SeoIssue(
                    file,
                    Severity.INFO,
                    "No tags or keywords - consider adding for better discoverability"
                ))
            }

            // Check content body
            val bodyContent = content.substringAfter("---\n").substringAfter("---\n")

            // Check for headings
            if (!bodyContent.contains("# ") && !bodyContent.contains("## ")) {
                issues.add(SeoIssue(
                    file,
                    Severity.INFO,
                    "No headings found - consider using H1/H2 for better structure"
                ))
            }

            // Check content length
            val wordCount = bodyContent.split("\\s+".toRegex()).size
            if (wordCount < 300) {
                issues.add(SeoIssue(
                    file,
                    Severity.INFO,
                    "Short content ($wordCount words) - longer content often ranks better"
                ))
            }

        } catch (e: IOException) {
            issues.add(SeoIssue(
                file,
                Severity.ERROR,
                "Failed to read file: ${e.message}"
            ))
        }

        return issues
    }

    private fun extractFrontmatter(content: String): Map<String, String> {
        if (!content.trimStart().startsWith("---")) {
            return emptyMap()
        }

        val parts = content.trim().split("---")
        if (parts.size < 3) {
            return emptyMap()
        }

        val frontmatterText = parts[1]
        val map = mutableMapOf<String, String>()

        frontmatterText.lines().forEach { line ->
            val colonIndex = line.indexOf(':')
            if (colonIndex > 0) {
                val key = line.substring(0, colonIndex).trim()
                val value = line.substring(colonIndex + 1).trim().removeSurrounding("\"")
                map[key] = value
            }
        }

        return map
    }
}
