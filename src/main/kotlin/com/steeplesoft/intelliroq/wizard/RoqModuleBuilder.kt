package com.steeplesoft.intelliroq.wizard

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.ui.Messages
import com.steeplesoft.intelliroq.icons.RoqIcons

/**
 * Module builder for creating new Roq projects via File | New | Project.
 * Currently stubbed - full implementation pending.
 */
class RoqModuleBuilder : ModuleBuilder() {

    override fun getModuleType(): ModuleType<*> = RoqModuleType.INSTANCE

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        // Stub: Basic module setup
        super.setupRootModel(modifiableRootModel)

        doAddContentEntry(modifiableRootModel)

        // TODO: Add Roq project structure initialization
        // TODO: Add Maven/Gradle build file generation
        // TODO: Add initial content and templates
    }

    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep {
        return RoqProjectWizardStep(this)
    }

    override fun getBuilderId(): String = "roq.project"

    override fun getPresentableName(): String = "Roq Static Site"

    override fun getDescription(): String = "Create a new Roq static site generator project"

    override fun getNodeIcon() = RoqIcons.RoqProject

    override fun getGroupName(): String = "Quarkus"

    override fun getWeight(): Int = 50

    /**
     * Placeholder wizard step.
     */
    private class RoqProjectWizardStep(private val builder: RoqModuleBuilder) : ModuleWizardStep() {

        override fun getComponent() = javax.swing.JPanel().apply {
            layout = java.awt.BorderLayout()

            val label = javax.swing.JLabel(
                "<html><body style='padding: 20px;'>" +
                "<h2>Roq Project Creation</h2>" +
                "<p>This wizard is currently under development.</p>" +
                "<p>For now, please use:</p>" +
                "<ul>" +
                "<li><b>Tools > Roq > Initialize Roq Project Structure</b> to add Roq to an existing project</li>" +
                "</ul>" +
                "</body></html>"
            )
            add(label, java.awt.BorderLayout.CENTER)
        }

        override fun updateDataModel() {
            // Stub: No data to update yet
        }

        override fun validate(): Boolean {
            Messages.showInfoMessage(
                "Roq project wizard is coming soon!\n\n" +
                "For now, create a Java project and use:\n" +
                "Tools > Roq > Initialize Roq Project Structure",
                "Roq Project Wizard - Coming Soon"
            )
            return false // Prevent project creation for now
        }
    }
}
