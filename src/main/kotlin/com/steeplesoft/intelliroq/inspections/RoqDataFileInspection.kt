package com.steeplesoft.intelliroq.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException

/**
 * Inspection for Roq data files (YAML/JSON validation).
 */
class RoqDataFileInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                super.visitFile(file)

                // Only inspect files in data/ directory
                if (!file.virtualFile.path.contains("/data/")) {
                    return
                }

                // Validate YAML files
                if (file.name.endsWith(".yml") || file.name.endsWith(".yaml")) {
                    validateYaml(file, holder)
                }

                // JSON validation is handled by built-in IntelliJ support
            }
        }
    }

    private fun validateYaml(file: PsiFile, holder: ProblemsHolder) {
        try {
            val yaml = Yaml()
            yaml.load<Any>(file.text)
        } catch (e: YAMLException) {
            // YAML validation errors are already shown by IntelliJ's YAML plugin
            // This is just a placeholder for custom Roq-specific validations
        }
    }
}
