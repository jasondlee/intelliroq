package com.steeplesoft.roqidea.fileTypes

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

/**
 * File type for Roq content files (Markdown in content directory).
 */
class RoqContentFileType : LanguageFileType(com.intellij.lang.Language.ANY), FileTypeIdentifiableByVirtualFile {

    override fun getName(): @NonNls String = "Roq Content"

    override fun getDescription(): String = "Roq content file"

    override fun getDefaultExtension(): String = "md"

    override fun getIcon(): Icon? = com.intellij.icons.AllIcons.FileTypes.Text

    override fun isMyFileType(file: VirtualFile): Boolean {
        // Only recognize .md files in content/ directory as Roq content
        return file.path.contains("/content/") && file.extension == "md"
    }

    companion object {
        @JvmField
        val INSTANCE = RoqContentFileType()
    }
}

/**
 * File type for Qute template files.
 */
class QuteTemplateFileType : LanguageFileType(com.intellij.lang.Language.ANY), FileTypeIdentifiableByVirtualFile {

    override fun getName(): @NonNls String = "Qute Template"

    override fun getDescription(): String = "Qute template file"

    override fun getDefaultExtension(): String = "html"

    override fun getIcon(): Icon? = com.intellij.icons.AllIcons.FileTypes.Html

    override fun isMyFileType(file: VirtualFile): Boolean {
        // Recognize .html files in templates/ directory as Qute templates
        return file.path.contains("/templates/") && file.extension == "html"
    }

    companion object {
        @JvmField
        val INSTANCE = QuteTemplateFileType()
    }
}
