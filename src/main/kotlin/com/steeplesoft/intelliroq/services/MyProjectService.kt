package com.steeplesoft.intelliroq.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.steeplesoft.intelliroq.MyBundle

@Service(Service.Level.PROJECT)
class MyProjectService(private val project: Project) {

    private val detector: RoqProjectDetector = project.service()

    init {
        thisLogger().info(MyBundle.message("projectService", project.name))
    }

    /**
     * Checks if this project is a Roq project.
     */
    fun isRoqProject(): Boolean = detector.isRoqProject()

    /**
     * Gets the Roq project detector service.
     */
    fun getDetector(): RoqProjectDetector = detector
}
