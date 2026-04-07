package com.steeplesoft.roqidea.lang

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext

/**
 * Contributor that registers reference providers for Roq include statements.
 * Handles patterns like {#include partials/foo /} in .adoc, .md, and .html files.
 */
class QuteTemplateReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val provider = RoqIncludeReferenceProvider()

        // Register for all PSI elements - we'll filter in the provider
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(),
            provider
        )
    }
}

/**
 * Reference provider for Roq include statements.
 * Matches patterns like {#include partials/foo /} and creates references to the included files.
 */
class RoqIncludeReferenceProvider : PsiReferenceProvider() {
    companion object {
        // Pattern to match {#include partials/foo /} or {#include partials/foo.html /}
        private val INCLUDE_PATTERN = Regex("""\{#include\s+(\S+?)\s*/}""")
        private val SUPPORTED_EXTENSIONS = setOf("html", "adoc", "md", "qute")
    }

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        // Only process elements in supported file types
        val file = element.containingFile ?: return PsiReference.EMPTY_ARRAY
        val fileExtension = file.virtualFile?.extension ?: return PsiReference.EMPTY_ARRAY
        if (fileExtension !in SUPPORTED_EXTENSIONS) {
            return PsiReference.EMPTY_ARRAY
        }

        val text = element.text ?: return PsiReference.EMPTY_ARRAY

        // Skip if text doesn't contain our include pattern
        if (!text.contains("{#include")) {
            return PsiReference.EMPTY_ARRAY
        }

        val references = mutableListOf<PsiReference>()

        // Find all include patterns in the text
        INCLUDE_PATTERN.findAll(text).forEach { matchResult ->
            val fullMatch = matchResult.value
            val pathGroup = matchResult.groups[1] ?: return@forEach
            val path = pathGroup.value

            // Calculate the text range of the path within the element
            val startOffset = matchResult.range.first + fullMatch.indexOf(path)
            val endOffset = startOffset + path.length
            val textRange = TextRange(startOffset, endOffset)

            references.add(RoqIncludeReference(element, textRange, path))
        }

        return references.toTypedArray()
    }
}

/**
 * PSI reference for Roq include statements.
 * Resolves paths like "partials/foo" to "templates/partials/foo.html" in the project.
 */
class RoqIncludeReference(element: PsiElement, textRange: TextRange, private val includePath: String) :
    PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {

    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.isNotEmpty()) results[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project

        // Extract the filename from the path (e.g., "partials/foo" -> "foo")
        val pathWithoutExtension = includePath.substringBeforeLast('.', includePath)
        val specifiedExtension = if (includePath.contains('.')) includePath.substringAfterLast('.') else null
        val filename = pathWithoutExtension.substringAfterLast('/')

        // Determine the subdirectory (e.g., "partials" or "layouts")
        val subdir = when {
            includePath.startsWith("partials/") -> "partials"
            includePath.startsWith("layouts/") -> "layouts"
            else -> "partials" // Default to partials
        }

        val results = mutableListOf<ResolveResult>()

        // Build list of filenames to search for
        val filenamesToTry = if (specifiedExtension != null) {
            listOf("$filename.$specifiedExtension")
        } else {
            listOf("$filename.html", "$filename.qute.html", "$filename.qute", "$filename.adoc", "$filename.md")
        }

        // Search for each filename using IntelliJ's FilenameIndex
        val scope = GlobalSearchScope.projectScope(project)
        for (filenameToTry in filenamesToTry) {
            val virtualFiles = FilenameIndex.getVirtualFilesByName(filenameToTry, scope)
            val psiManager = PsiManager.getInstance(project)
            val psiFiles = virtualFiles.mapNotNull { psiManager.findFile(it) }

            // Filter to only files in the correct templates subdirectory
            psiFiles.forEach { psiFile ->
                val filePath = psiFile.virtualFile?.path ?: return@forEach
                if (filePath.contains("templates/$subdir/$filenameToTry") ||
                    filePath.endsWith("/$subdir/$filenameToTry")) {
                    results.add(PsiElementResolveResult(psiFile))
                }
            }

            // If we found matches, return them (don't keep searching with other extensions)
            if (results.isNotEmpty()) {
                break
            }
        }

        return results.toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        // Could return completion variants for available partials/layouts
        return emptyArray()
    }
}
