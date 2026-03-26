package com.steeplesoft.intelliroq

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.steeplesoft.intelliroq.services.RoqProjectDetector

class RoqProjectDetectorTest : BasePlatformTestCase() {

    fun testDetectorService() {
        val detector = project.service<RoqProjectDetector>()
        assertNotNull(detector)
    }

    fun testIsNotRoqProject() {
        val detector = project.service<RoqProjectDetector>()
        // Empty project should not be detected as Roq project
        assertFalse(detector.isRoqProject())
    }

    fun testHasRoqStructure() {
        val detector = project.service<RoqProjectDetector>()
        // Initially should not have Roq structure
        assertFalse(detector.hasRoqStructure())
    }

    fun testGetRoqDirectories() {
        val detector = project.service<RoqProjectDetector>()
        val directories = detector.getRoqDirectories()

        assertNotNull(directories)
        assertNull(directories.contentDir)
        assertNull(directories.templatesDir)
        assertNull(directories.dataDir)
    }
}
