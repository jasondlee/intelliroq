package com.steeplesoft.roqidea

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.steeplesoft.roqidea.services.RoqPluginManager

class RoqPluginManagerTest : BasePlatformTestCase() {

    fun testPluginManagerService() {
        val pluginManager = project.service<RoqPluginManager>()
        assertNotNull(pluginManager)
    }

    fun testGetAvailablePlugins() {
        val pluginManager = project.service<RoqPluginManager>()
        val availablePlugins = pluginManager.getAvailablePlugins()

        assertNotEmpty(availablePlugins)
        assertTrue(availablePlugins.size >= 10) // At least 10 known plugins
    }

    fun testPluginDataStructure() {
        val pluginManager = project.service<RoqPluginManager>()
        val availablePlugins = pluginManager.getAvailablePlugins()

        // Check markdown plugin specifically
        val markdownPlugin = availablePlugins.find { it.name == "markdown" }
        assertNotNull(markdownPlugin)
        assertEquals("quarkus-roq-plugin-markdown", markdownPlugin!!.artifactId)
        assertEquals("Markdown", markdownPlugin.displayName)
        assertTrue(markdownPlugin.isCore)
    }

    fun testGetInstalledPluginsEmpty() {
        val pluginManager = project.service<RoqPluginManager>()
        val installedPlugins = pluginManager.getInstalledPlugins()

        // Empty project should have no plugins
        assertTrue(installedPlugins.isEmpty())
    }

    fun testIsPluginInstalledEmpty() {
        val pluginManager = project.service<RoqPluginManager>()

        // Empty project should have no plugins installed
        assertFalse(pluginManager.isPluginInstalled("markdown"))
        assertFalse(pluginManager.isPluginInstalled("tagging"))
    }

    fun testGetRoqVersionEmpty() {
        val pluginManager = project.service<RoqPluginManager>()
        val version = pluginManager.getRoqVersion()

        // Empty project should have no Roq version
        assertNull(version)
    }

    fun testGetAvailableUninstalledPlugins() {
        val pluginManager = project.service<RoqPluginManager>()
        val uninstalled = pluginManager.getAvailableUninstalledPlugins()

        // In empty project, all should be uninstalled
        assertEquals(pluginManager.getAvailablePlugins().size, uninstalled.size)
    }

    fun testPluginDisplayNames() {
        val pluginManager = project.service<RoqPluginManager>()
        val availablePlugins = pluginManager.getAvailablePlugins()

        val asciidocJRuby = availablePlugins.find { it.name == "asciidoc-jruby" }
        assertNotNull(asciidocJRuby)
        assertEquals("Asciidoc Jruby", asciidocJRuby!!.displayName)
    }
}
