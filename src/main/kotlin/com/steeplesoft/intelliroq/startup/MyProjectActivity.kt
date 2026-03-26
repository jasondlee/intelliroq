package com.steeplesoft.intelliroq.startup

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.steeplesoft.intelliroq.services.MyProjectService

class MyProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val projectService = project.service<MyProjectService>()
        val isRoq = projectService.isRoqProject()

        if (isRoq) {
            thisLogger().info("IntelliRoq plugin initialized for Roq project: ${project.name}")
        } else {
            thisLogger().info("IntelliRoq plugin initialized for non-Roq project: ${project.name}")
        }
    }
}
