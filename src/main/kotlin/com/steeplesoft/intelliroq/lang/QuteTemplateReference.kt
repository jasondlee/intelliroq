package com.steeplesoft.intelliroq.lang

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext

/**
 * Contributor that registers the Qute template reference provider.
 */
class QuteTemplateReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Registration logic can be added here if needed
        // For now, this is a placeholder that satisfies the plugin.xml requirement
    }
}

/**
 * Reference provider for Qute template includes and partial references.
 */
class QuteTemplateReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (element !is PsiLiteralValue) {
            return PsiReference.EMPTY_ARRAY
        }

        val value = element.value as? String ?: return PsiReference.EMPTY_ARRAY

        // Match {#include partials/header /} or similar patterns
        if (value.contains("partials/") || value.contains("layouts/")) {
            return arrayOf(QuteTemplateReference(element, TextRange(1, value.length + 1)))
        }

        return PsiReference.EMPTY_ARRAY
    }
}

class QuteTemplateReference(element: PsiElement, textRange: TextRange) :
    PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {

    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project
        val value = value

        // Extract filename from path (e.g., "partials/header.html" -> "header.html")
        val filename = value.substringAfterLast('/')

        // Search for the file in the project
        val files = FilenameIndex.getFilesByName(
            project,
            filename,
            GlobalSearchScope.projectScope(project)
        )

        return files.map { PsiElementResolveResult(it) }.toTypedArray()
    }
}
