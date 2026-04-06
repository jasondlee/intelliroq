package com.steeplesoft.roqidea.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import java.io.IOException

/**
 * Service for managing Roq plugins in the project.
 */
@Service(Service.Level.PROJECT)
class RoqPluginManager(private val project: Project) {

    companion object {
        const val ROQ_GROUP_ID = "io.quarkiverse.roq"
        const val ROQ_PLUGIN_PREFIX = "quarkus-roq-plugin-"
        const val ROQ_CORE_ARTIFACT = "quarkus-roq"
        const val DEFAULT_ROQ_VERSION = "2.0.4"

        /**
         * Known Roq plugins with descriptions.
         */
        val KNOWN_PLUGINS = mapOf(
            "markdown" to RoqPlugin("markdown", "Process CommonMark content (included by default)", true),
            "tagging" to RoqPlugin("tagging", "Generate dynamic tag collection pages"),
            "aliases" to RoqPlugin("aliases", "Create URL redirections"),
            "asciidoc-jruby" to RoqPlugin("asciidoc-jruby", "Asciidoctor markup support (JRuby)"),
            "asciidoc" to RoqPlugin("asciidoc", "AsciiDoc support (Java-based)"),
            "qrcode" to RoqPlugin("qrcode", "Embed QR codes in templates"),
            "series" to RoqPlugin("series", "Group related posts together"),
            "sitemap" to RoqPlugin("sitemap", "Auto-generate sitemap.xml"),
            "lunr" to RoqPlugin("lunr", "Client-side search indexing"),
            "diagram" to RoqPlugin("diagram", "Render diagrams via Kroki.io")
        )
    }

    /**
     * Data class representing a Roq plugin.
     */
    data class RoqPlugin(
        val name: String,
        val description: String,
        val isCore: Boolean = false
    ) {
        val artifactId: String
            get() = "$ROQ_PLUGIN_PREFIX$name"

        val displayName: String
            get() = name.split("-").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }

    /**
     * Data class representing an installed plugin with version information.
     */
    data class InstalledPlugin(
        val plugin: RoqPlugin,
        val version: String?,
        val buildFile: String
    )

    /**
     * Detects all installed Roq plugins in the project.
     */
    fun getInstalledPlugins(): List<InstalledPlugin> {
        val plugins = mutableListOf<InstalledPlugin>()

        // Check Maven
        plugins.addAll(getInstalledMavenPlugins())

        // Check Gradle
        plugins.addAll(getInstalledGradlePlugins())

        return plugins
    }

    /**
     * Checks if a specific plugin is installed.
     */
    fun isPluginInstalled(pluginName: String): Boolean {
        return getInstalledPlugins().any { it.plugin.name == pluginName }
    }

    /**
     * Gets the Roq version used in the project.
     */
    fun getRoqVersion(): String? {
        // Try to detect from Maven
        val mavenVersion = getRoqVersionFromMaven()
        if (mavenVersion != null) return mavenVersion

        // Try to detect from Gradle
        val gradleVersion = getRoqVersionFromGradle()
        if (gradleVersion != null) return gradleVersion

        return null
    }

    /**
     * Detects installed plugins from Maven pom.xml.
     */
    private fun getInstalledMavenPlugins(): List<InstalledPlugin> {
        val plugins = mutableListOf<InstalledPlugin>()
        val pomFiles = FilenameIndex.getFilesByName(
            project,
            "pom.xml",
            GlobalSearchScope.projectScope(project)
        )

        pomFiles.forEach { pomFile ->
            try {
                val content = String(pomFile.virtualFile.contentsToByteArray())

                // Parse each known plugin
                KNOWN_PLUGINS.forEach { (name, plugin) ->
                    if (content.contains(plugin.artifactId)) {
                        val version = extractVersionFromMaven(content, plugin.artifactId)
                        plugins.add(InstalledPlugin(plugin, version, pomFile.virtualFile.path))
                    }
                }
            } catch (e: IOException) {
                // Ignore
            }
        }

        return plugins
    }

    /**
     * Detects installed plugins from Gradle build files.
     */
    private fun getInstalledGradlePlugins(): List<InstalledPlugin> {
        val plugins = mutableListOf<InstalledPlugin>()
        val buildFiles = listOf("build.gradle", "build.gradle.kts")

        buildFiles.forEach { filename ->
            val files = FilenameIndex.getFilesByName(
                project,
                filename,
                GlobalSearchScope.projectScope(project)
            )

            files.forEach { buildFile ->
                try {
                    val content = String(buildFile.virtualFile.contentsToByteArray())

                    // Parse each known plugin
                    KNOWN_PLUGINS.forEach { (name, plugin) ->
                        if (content.contains(plugin.artifactId)) {
                            val version = extractVersionFromGradle(content, plugin.artifactId)
                            plugins.add(InstalledPlugin(plugin, version, buildFile.virtualFile.path))
                        }
                    }
                } catch (e: IOException) {
                    // Ignore
                }
            }
        }

        return plugins
    }

    /**
     * Extracts version from Maven dependency declaration.
     */
    private fun extractVersionFromMaven(content: String, artifactId: String): String? {
        // Look for <artifactId>artifact</artifactId> followed by <version>X.X.X</version>
        val pattern = "<artifactId>$artifactId</artifactId>\\s*<version>([^<]+)</version>".toRegex()
        val match = pattern.find(content)
        return match?.groupValues?.get(1)?.trim()
    }

    /**
     * Extracts version from Gradle dependency declaration.
     */
    private fun extractVersionFromGradle(content: String, artifactId: String): String? {
        // Look for "io.quarkiverse.roq:artifact:version"
        val pattern = "\"$ROQ_GROUP_ID:$artifactId:([^\"]+)\"".toRegex()
        val match = pattern.find(content)
        if (match != null) {
            return match.groupValues[1].trim()
        }

        // Try single quote variant
        val singleQuotePattern = "'$ROQ_GROUP_ID:$artifactId:([^']+)'".toRegex()
        val singleQuoteMatch = singleQuotePattern.find(content)
        return singleQuoteMatch?.groupValues?.get(1)?.trim()
    }

    /**
     * Gets Roq core version from Maven.
     */
    private fun getRoqVersionFromMaven(): String? {
        val pomFiles = FilenameIndex.getFilesByName(
            project,
            "pom.xml",
            GlobalSearchScope.projectScope(project)
        )

        pomFiles.forEach { pomFile ->
            try {
                val content = String(pomFile.virtualFile.contentsToByteArray())
                val version = extractVersionFromMaven(content, ROQ_CORE_ARTIFACT)
                if (version != null) return version
            } catch (e: IOException) {
                // Ignore
            }
        }

        return null
    }

    /**
     * Gets Roq core version from Gradle.
     */
    private fun getRoqVersionFromGradle(): String? {
        val buildFiles = listOf("build.gradle", "build.gradle.kts")

        buildFiles.forEach { filename ->
            val files = FilenameIndex.getFilesByName(
                project,
                filename,
                GlobalSearchScope.projectScope(project)
            )

            files.forEach { buildFile ->
                try {
                    val content = String(buildFile.virtualFile.contentsToByteArray())
                    val version = extractVersionFromGradle(content, ROQ_CORE_ARTIFACT)
                    if (version != null) return version
                } catch (e: IOException) {
                    // Ignore
                }
            }
        }

        return null
    }

    /**
     * Gets all available Roq plugins (both installed and not installed).
     */
    fun getAvailablePlugins(): List<RoqPlugin> {
        return KNOWN_PLUGINS.values.toList()
    }

    /**
     * Gets plugins that are available but not yet installed.
     */
    fun getAvailableUninstalledPlugins(): List<RoqPlugin> {
        val installed = getInstalledPlugins().map { it.plugin.name }.toSet()
        return KNOWN_PLUGINS.values.filter { it.name !in installed }
    }
}
