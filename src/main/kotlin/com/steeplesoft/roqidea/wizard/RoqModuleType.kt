package com.steeplesoft.roqidea.wizard

import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.steeplesoft.roqidea.icons.RoqIcons
import javax.swing.Icon

/**
 * Module type for Roq projects.
 */
class RoqModuleType : ModuleType<RoqModuleBuilder>("ROQ_MODULE") {

    override fun createModuleBuilder(): RoqModuleBuilder = RoqModuleBuilder()

    override fun getName(): String = "Roq Static Site"

    override fun getDescription(): String = "Roq static site generator powered by Quarkus"

    override fun getNodeIcon(isOpened: Boolean): Icon = RoqIcons.RoqProject

    companion object {
        const val ID = "ROQ_MODULE"

        val INSTANCE: RoqModuleType
            get() = ModuleTypeManager.getInstance().findByID(ID) as RoqModuleType
    }
}
