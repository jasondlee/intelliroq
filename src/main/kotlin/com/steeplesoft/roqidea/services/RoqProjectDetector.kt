package com.steeplesoft.roqidea.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import java.io.IOException

/**
 * Service for detecting whether a project is a Roq static site generator project.
 */
@Service(Service.Level.PROJECT)
class RoqProjectDetector(private val project: Project) {

    /**
     * Checks if the current project is a Roq project by examining:
     * - Presence of content/ directory
     * - Roq dependencies in build files
     * - Roq configuration properties
     */
    fun isRoqProject(): Boolean {
        return hasRoqDependencies() || hasRoqConfiguration() || hasRoqStructure()
    }

    /**
     * Checks if the project has the typical Roq directory structure.
     */
    fun hasRoqStructure(): Boolean {
        val baseDir = project.baseDir ?: return false

        // content/ is the most reliable indicator of a Roq project
        val contentDir = baseDir.findChild("content")
        if (contentDir != null && contentDir.isDirectory) {
            return true
        }

        return false
    }

    /**
     * Checks if the project has Roq-specific configuration properties.
     */
    fun hasRoqConfiguration(): Boolean {
        val applicationProperties = findApplicationProperties() ?: return false

        try {
            val content = String(applicationProperties.virtualFile.contentsToByteArray())

            // Check for Roq-specific properties
            return content.contains("quarkus.roq") ||
                   content.contains("site.url") ||
                   content.contains("site.content-dir") ||
                   content.contains("site.collections")
        } catch (e: IOException) {
            return false
        }
    }

    /**
     * Checks if the project has Roq dependencies in Maven or Gradle build files.
     */
    fun hasRoqDependencies(): Boolean {
        return hasMavenRoqDependencies() || hasGradleRoqDependencies()
    }

    /**
     * Checks Maven pom.xml for Roq dependencies.
     */
    private fun hasMavenRoqDependencies(): Boolean {
        val pomFiles = FilenameIndex.getFilesByName(
            project,
            "pom.xml",
            GlobalSearchScope.projectScope(project)
        )

        return pomFiles.any { pomFile ->
            try {
                val content = String(pomFile.virtualFile.contentsToByteArray())
                content.contains("io.quarkiverse.roq") &&
                (content.contains("quarkus-roq") || content.contains("quarkus-roq-plugin"))
            } catch (e: IOException) {
                false
            }
        }
    }

    /**
     * Checks Gradle build files for Roq dependencies.
     */
    private fun hasGradleRoqDependencies(): Boolean {
        val buildFiles = listOf("build.gradle", "build.gradle.kts")

        return buildFiles.any { filename ->
            val files = FilenameIndex.getFilesByName(
                project,
                filename,
                GlobalSearchScope.projectScope(project)
            )

            files.any { buildFile ->
                try {
                    val content = String(buildFile.virtualFile.contentsToByteArray())
                    content.contains("io.quarkiverse.roq") &&
                    (content.contains("quarkus-roq") || content.contains("quarkus-roq-plugin"))
                } catch (e: IOException) {
                    false
                }
            }
        }
    }

    /**
     * Finds application.properties file in the project.
     */
    private fun findApplicationProperties(): PsiFile? {
        val files = FilenameIndex.getFilesByName(
            project,
            "application.properties",
            GlobalSearchScope.projectScope(project)
        )

        // Prefer the one in src/main/resources
        return files.firstOrNull { it.virtualFile.path.contains("src/main/resources") } ?: files.firstOrNull()
    }

    /**
     * Gets Roq-related directories in the project.
     */
    fun getRoqDirectories(): RoqDirectories {
        val baseDir = project.baseDir

        return RoqDirectories(
            contentDir = baseDir?.findChild("content"),
            templatesDir = baseDir?.findChild("templates"),
            dataDir = baseDir?.findChild("data"),
            staticDir = baseDir?.findChild("static"),
            publicDir = baseDir?.findChild("public"),
            webDir = baseDir?.findChild("web")
        )
    }

    /**
     * Data class holding references to Roq-related directories.
     */
    data class RoqDirectories(
        val contentDir: VirtualFile?,
        val templatesDir: VirtualFile?,
        val dataDir: VirtualFile?,
        val staticDir: VirtualFile?,
        val publicDir: VirtualFile?,
        val webDir: VirtualFile?
    )
}
