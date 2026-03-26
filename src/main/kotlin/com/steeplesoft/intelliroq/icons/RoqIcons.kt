package com.steeplesoft.intelliroq.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * Icons for the Roq plugin.
 */
object RoqIcons {
    @JvmField
    val RoqFile: Icon = IconLoader.getIcon("/icons/roq-file.svg", RoqIcons::class.java)

    @JvmField
    val RoqProject: Icon = IconLoader.getIcon("/icons/roq-project.svg", RoqIcons::class.java)

    @JvmField
    val DataFile: Icon = IconLoader.getIcon("/icons/data-file.svg", RoqIcons::class.java)

    @JvmField
    val Template: Icon = IconLoader.getIcon("/icons/template.svg", RoqIcons::class.java)

    @JvmField
    val Partial: Icon = IconLoader.getIcon("/icons/partial.svg", RoqIcons::class.java)
}
