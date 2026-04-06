package com.steeplesoft.roqidea.wizard

import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException

/**
 * Utility for initializing Roq project structure and creating sample files.
 */
object RoqProjectInitializer {

    /**
     * Creates the standard Roq directory structure.
     */
    fun createDirectoryStructure(baseDir: VirtualFile) {
        // Create main directories
        val contentDir = baseDir.createChildDirectory(null, "content")
        val templatesDir = baseDir.createChildDirectory(null, "templates")
        baseDir.createChildDirectory(null, "data")
        baseDir.createChildDirectory(null, "static")
        baseDir.createChildDirectory(null, "public")

        // Create template subdirectories
        templatesDir.createChildDirectory(null, "layouts")
        templatesDir.createChildDirectory(null, "partials")
    }

    /**
     * Creates sample content and data files.
     */
    fun createSampleFiles(baseDir: VirtualFile, siteUrl: String) {
        val contentDir = baseDir.findChild("content") ?: throw IOException("content/ directory not found")
        val dataDir = baseDir.findChild("data") ?: throw IOException("data/ directory not found")

        createIndexPage(contentDir)
        createSampleData(dataDir)
    }

    /**
     * Creates or updates application.properties with Roq configuration.
     */
    fun createApplicationProperties(baseDir: VirtualFile, siteUrl: String) {
        val resourcesDir = getOrCreateResourcesDir(baseDir)
        val propsFile = resourcesDir.findChild("application.properties")

        val roqConfig = generateApplicationPropertiesContent(siteUrl)

        if (propsFile == null) {
            // Create new file
            val newFile = resourcesDir.createChildData(null, "application.properties")
            newFile.setBinaryContent(roqConfig.toByteArray())
        } else {
            // Append to existing file
            val existingContent = String(propsFile.contentsToByteArray())
            if (!existingContent.contains("quarkus.roq")) {
                val updatedContent = existingContent.trimEnd() + "\n\n" + roqConfig
                propsFile.setBinaryContent(updatedContent.toByteArray())
            }
        }
    }

    /**
     * Generates application.properties content with Roq configuration.
     */
    fun generateApplicationPropertiesContent(siteUrl: String): String {
        return """
# Roq Configuration
# See: https://docs.quarkiverse.io/quarkus-roq/dev/index.html

# Site metadata
site.url=$siteUrl
site.title=My Roq Site
site.description=A static site built with Roq

# Collections
site.collections."posts".enabled=true
site.collections."posts".layout=:theme/post
site.collections."posts".future=false

# Generator configuration (for static build)
quarkus.roq.generator.paths=/,/posts/**
quarkus.roq.generator.output-dir=roq

        """.trimIndent()
    }

    /**
     * Creates a sample index page.
     */
    private fun createIndexPage(contentDir: VirtualFile) {
        val indexFile = contentDir.createChildData(null, "index.md")
        val content = """
---
layout: :theme/page
title: Welcome to Roq
---

# Welcome to Roq

This is your Roq static site generator project!

## Getting Started

1. Run in dev mode: `quarkus dev` (or `./mvnw quarkus:dev` / `./gradlew quarkusDev`)
2. Open your browser to http://localhost:8080
3. Start creating content!

## Project Structure

- `content/` - Your pages and blog posts (Markdown, AsciiDoc, HTML)
- `templates/` - Custom layouts and partials (Qute templates)
- `data/` - Structured data files (YAML, JSON)
- `static/` - Static assets (images, downloads)
- `public/` - Public assets served at root

Happy coding!
        """.trimIndent()

        indexFile.setBinaryContent(content.toByteArray())
    }

    /**
     * Creates a sample data file.
     */
    private fun createSampleData(dataDir: VirtualFile) {
        val menuFile = dataDir.createChildData(null, "menu.yml")
        val content = """
# Main navigation menu
items:
  - name: Home
    url: /
  - name: Blog
    url: /posts/
  - name: About
    url: /about/
        """.trimIndent()

        menuFile.setBinaryContent(content.toByteArray())
    }

    /**
     * Gets or creates src/main/resources directory.
     */
    private fun getOrCreateResourcesDir(baseDir: VirtualFile): VirtualFile {
        var srcDir = baseDir.findChild("src")
        if (srcDir == null) {
            srcDir = baseDir.createChildDirectory(null, "src")
        }

        var mainDir = srcDir.findChild("main")
        if (mainDir == null) {
            mainDir = srcDir.createChildDirectory(null, "main")
        }

        var resourcesDir = mainDir.findChild("resources")
        if (resourcesDir == null) {
            resourcesDir = mainDir.createChildDirectory(null, "resources")
        }

        return resourcesDir
    }
}
