package com.steeplesoft.roqidea.lang

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

/**
 * Code completion contributor for Qute templates.
 */
class QuteCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            QuteCompletionProvider()
        )
    }
}

class QuteCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val file = parameters.originalFile

        // Only provide completions in template files
        if (!isTemplateFile(file.name)) {
            return
        }

        // Add common Qute directives
        addQuteDirectives(result)

        // Add Roq-specific template variables
        addRoqVariables(result)
    }

    private fun isTemplateFile(filename: String): Boolean {
        return filename.endsWith(".html") || filename.endsWith(".qute.html")
    }

    private fun addQuteDirectives(result: CompletionResultSet) {
        val directives = listOf(
            "if", "else", "for", "each", "let", "set",
            "include", "insert", "fragment", "switch", "case",
            "when", "with"
        )

        directives.forEach { directive ->
            result.addElement(
                LookupElementBuilder.create("{#$directive}")
                    .withPresentableText("#$directive")
                    .withTypeText("Qute directive")
                    .withInsertHandler { context, _ ->
                        // Add closing tag for block directives
                        if (directive in listOf("if", "for", "each", "let", "with")) {
                            val editor = context.editor
                            val document = editor.document
                            val offset = context.tailOffset
                            document.insertString(offset, "\n{/$directive}")
                            editor.caretModel.moveToOffset(offset)
                        }
                    }
            )
        }
    }

    private fun addRoqVariables(result: CompletionResultSet) {
        val variables = listOf(
            "site" to "Site-wide configuration and data",
            "page" to "Current page data and metadata",
            "site.collections.posts" to "Blog posts collection",
            "site.collections.docs" to "Documentation collection",
            "page.title" to "Page title",
            "page.data" to "Page frontmatter data",
            "page.draft" to "Draft status",
            "site.url" to "Site URL",
            "site.title" to "Site title"
        )

        variables.forEach { (variable, description) ->
            result.addElement(
                LookupElementBuilder.create(variable)
                    .withTypeText(description)
                    .withIcon(com.intellij.icons.AllIcons.Nodes.Variable)
            )
        }
    }
}
