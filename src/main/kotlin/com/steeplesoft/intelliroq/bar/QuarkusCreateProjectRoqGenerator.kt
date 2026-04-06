package com.steeplesoft.intelliroq.bar

import io.quarkus.devtools.commands.CreateProject
import io.quarkus.devtools.messagewriter.MessageWriter
import io.quarkus.devtools.project.BuildTool
import io.quarkus.devtools.project.CodestartResourceLoadersBuilder
import io.quarkus.devtools.project.JavaVersion
import io.quarkus.devtools.project.QuarkusProject
import io.quarkus.devtools.project.QuarkusProjectHelper
import io.quarkus.devtools.project.SourceType
import java.nio.file.Path

/**
 * Generator for Roq projects using the Quarkus CreateProject command API.
 *
 * This class demonstrates using the CreateProject command from the Quarkus devtools,
 * which is a higher-level API compared to the Codestart API. The CreateProject command
 * is used by the Quarkus CLI and provides a simpler interface for project generation.
 *
 * ## Usage Example
 * ```kotlin
 * val generator = QuarkusCreateProjectRoqGenerator()
 * val config = RoqProjectConfig(
 *     outputPath = Paths.get("/tmp/my-roq-site"),
 *     groupId = "com.example",
 *     artifactId = "my-roq-site"
 * )
 * generator.createRoqProject(config)
 * ```
 *
 * ## Comparison to Codestart API
 * - **CreateProject**: Higher-level command API, simpler to use, less customization
 * - **Codestart API**: Lower-level, more control over codestarts and templates
 *
 * Both approaches generate valid Quarkus projects with Roq extensions.
 *
 * @see <a href="https://github.com/quarkusio/quarkus/blob/main/independent-projects/tools/devtools-common/src/main/java/io/quarkus/devtools/commands/CreateProject.java">CreateProject</a>
 */
class QuarkusCreateProjectRoqGenerator {

    companion object {
        /** Default Quarkus platform version */
        const val QUARKUS_VERSION = "3.34.1"

        /** Default Roq extension version */
        const val ROQ_VERSION = "2.0.5"

        /** Maven group ID for Roq extensions */
        const val ROQ_GROUP_ID = "io.quarkiverse.roq"

        /** Core Roq extension artifact ID */
        const val ROQ_EXTENSION_ARTIFACT_ID = "quarkus-roq"

        /** Default Roq theme artifact ID */
        const val ROQ_THEME_DEFAULT_ARTIFACT_ID = "quarkus-roq-theme-default"
    }

    /**
     * Configuration for creating a Roq project.
     *
     * @property outputPath Directory where the project will be created
     * @property groupId Maven/Gradle group ID (e.g., "com.example")
     * @property artifactId Maven/Gradle artifact ID (e.g., "my-roq-site")
     * @property version Project version (e.g., "1.0.0-SNAPSHOT")
     * @property buildTool Build tool to use (MAVEN, GRADLE, or GRADLE_KOTLIN_DSL)
     * @property javaVersion Target Java version (e.g., "21")
     * @property quarkusVersion Quarkus platform version to use
     * @property roqVersion Roq extension version to use
     * @property includeDefaultTheme Whether to include the default Roq theme
     * @property additionalPlugins Set of additional Roq plugin names to include
     */
    data class RoqProjectConfig(
        val outputPath: Path,
        val groupId: String = "com.example",
        val artifactId: String = "my-roq-site",
        val version: String = "1.0.0-SNAPSHOT",
        val buildTool: BuildTool = BuildTool.MAVEN,
        val javaVersion: String = "21",
        val quarkusVersion: String = QUARKUS_VERSION,
        val roqVersion: String = ROQ_VERSION,
        val includeDefaultTheme: Boolean = true,
        val additionalPlugins: Set<String> = emptySet()
    )

    /**
     * Creates a new Roq project using the Quarkus CreateProject command.
     *
     * This method uses the CreateProject command API to generate a complete
     * Quarkus project with Roq extensions and plugins.
     *
     * @param config Project configuration
     * @throws io.quarkus.registry.RegistryResolutionException if extension catalog cannot be resolved
     * @throws io.quarkus.devtools.commands.handlers.ProjectCreationException if project generation fails
     */
    fun createRoqProject(config: RoqProjectConfig) {
        // Build list of extensions to include
        val extensions = buildExtensionList(config)

        // Get the extension catalog from the registry
        val catalogResolver = QuarkusProjectHelper.getCatalogResolver()
        val extensionCatalog = catalogResolver.resolveExtensionCatalog()

        // Build resource loaders for codestarts
        val messageWriter = MessageWriter.info()
        val resourceLoaders = CodestartResourceLoadersBuilder.codestartLoadersBuilder(messageWriter)
            .catalog(extensionCatalog)
            .build()

        // Compute the Java version (returns a string like "21")
        val javaVersionString = JavaVersion.computeJavaVersion(SourceType.JAVA, config.javaVersion)
        val javaVersion = JavaVersion(javaVersionString)

        // Create the QuarkusProject
        val quarkusProject = QuarkusProject.of(
            config.outputPath,
            extensionCatalog,
            resourceLoaders,
            messageWriter,
            config.buildTool,
            javaVersion
        )

        // Create and execute the CreateProject command
        val createProject = CreateProject(quarkusProject)
            .groupId(config.groupId)
            .artifactId(config.artifactId)
            .version(config.version)
            .javaVersion(javaVersionString)
            .extensions(extensions)

        createProject.execute()
    }

    /**
     * Builds the list of Roq extension artifacts to include as dependencies.
     *
     * At minimum, includes the core quarkus-roq extension. Optionally includes:
     * - Default theme (if configured)
     * - Additional Roq plugins (tagging, sitemap, etc.)
     *
     * @param config Project configuration
     * @return Set of extension coordinates as strings (groupId:artifactId)
     */
    private fun buildExtensionList(config: RoqProjectConfig): Set<String> {
        val extensions = mutableSetOf<String>()

        // Core Roq extension (required)
        extensions.add("$ROQ_GROUP_ID:$ROQ_EXTENSION_ARTIFACT_ID:${config.roqVersion}")

        // Optional: Default theme
        if (config.includeDefaultTheme) {
            extensions.add("$ROQ_GROUP_ID:$ROQ_THEME_DEFAULT_ARTIFACT_ID:${config.roqVersion}")
        }

        // Additional Roq plugins
        config.additionalPlugins.forEach { pluginName ->
            extensions.add("$ROQ_GROUP_ID:quarkus-roq-plugin-$pluginName:${config.roqVersion}")
        }

        return extensions
    }
}
