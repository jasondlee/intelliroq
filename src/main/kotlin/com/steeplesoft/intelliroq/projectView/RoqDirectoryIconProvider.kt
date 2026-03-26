package com.steeplesoft.intelliroq.projectView

import com.intellij.ide.IconProvider
import com.intellij.openapi.components.service
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.steeplesoft.intelliroq.services.RoqProjectDetector
import javax.swing.Icon

/**
 * Provides custom icons for Roq directories.
 */
class RoqDirectoryIconProvider : IconProvider() {

    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        if (element !is PsiDirectory) {
            return null
        }

        val project = element.project
        val detector = project.service<RoqProjectDetector>()

        if (!detector.isRoqProject()) {
            return null
        }

        return when (element.name) {
            "content" -> com.intellij.icons.AllIcons.Nodes.Folder
            "templates" -> com.intellij.icons.AllIcons.Nodes.Folder
            "layouts" -> com.intellij.icons.AllIcons.Nodes.Folder
            "partials" -> com.intellij.icons.AllIcons.Nodes.Folder
            "data" -> com.intellij.icons.AllIcons.Nodes.DataSchema
            "static" -> com.intellij.icons.AllIcons.Nodes.ResourceBundle
            "public" -> com.intellij.icons.AllIcons.Nodes.ResourceBundle
            else -> null
        }
    }
}
