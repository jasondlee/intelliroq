package com.steeplesoft.roqidea.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement

/**
 * Documentation provider for Qute templates and Roq variables.
 */
class QuteDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val text = element?.text ?: return null

        return when {
            text.contains("site.") -> getSiteVariableDoc(text)
            text.contains("page.") -> getPageVariableDoc(text)
            text.startsWith("{#") -> getQuteDirectiveDoc(text)
            else -> null
        }
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return generateDoc(element, originalElement)
    }

    private fun getSiteVariableDoc(text: String): String {
        return when {
            "site.url" in text -> """
                <html><body>
                <b>site.url</b> - Site URL<br/>
                <p>The base URL of the site as configured in application.properties</p>
                <p>Example: <code>https://example.com</code></p>
                </body></html>
            """.trimIndent()

            "site.title" in text -> """
                <html><body>
                <b>site.title</b> - Site Title<br/>
                <p>The title of the site as configured in application.properties</p>
                </body></html>
            """.trimIndent()

            "site.collections.posts" in text -> """
                <html><body>
                <b>site.collections.posts</b> - Posts Collection<br/>
                <p>Access to all blog posts in the content/posts directory</p>
                <p>Usage: <code>{#for post in site.collections.posts}...{/for}</code></p>
                </body></html>
            """.trimIndent()

            else -> """
                <html><body>
                <b>site</b> - Site Object<br/>
                <p>Global site configuration and data</p>
                <p>Properties: url, title, description, collections, etc.</p>
                </body></html>
            """.trimIndent()
        }
    }

    private fun getPageVariableDoc(text: String): String {
        return when {
            "page.title" in text -> """
                <html><body>
                <b>page.title</b> - Page Title<br/>
                <p>The title of the current page from frontmatter</p>
                </body></html>
            """.trimIndent()

            "page.data" in text -> """
                <html><body>
                <b>page.data</b> - Page Frontmatter<br/>
                <p>All frontmatter data from the current page</p>
                <p>Access custom fields: <code>{page.data.author}</code></p>
                </body></html>
            """.trimIndent()

            "page.draft" in text -> """
                <html><body>
                <b>page.draft</b> - Draft Status<br/>
                <p>Boolean indicating if page is a draft</p>
                </body></html>
            """.trimIndent()

            else -> """
                <html><body>
                <b>page</b> - Page Object<br/>
                <p>Current page data and metadata</p>
                <p>Properties: title, date, draft, data, content, etc.</p>
                </body></html>
            """.trimIndent()
        }
    }

    private fun getQuteDirectiveDoc(text: String): String {
        val directive = text.substringAfter("{#").substringBefore(" ").substringBefore("}")

        return when (directive) {
            "if" -> """
                <html><body>
                <b>{#if condition}</b> - Conditional Block<br/>
                <p>Renders content if condition is true</p>
                <p>Example: <code>{#if page.draft}Draft{/if}</code></p>
                </body></html>
            """.trimIndent()

            "for" -> """
                <html><body>
                <b>{#for item in collection}</b> - Loop Block<br/>
                <p>Iterates over a collection</p>
                <p>Example: <code>{#for post in site.collections.posts}{post.title}{/for}</code></p>
                </body></html>
            """.trimIndent()

            "include" -> """
                <html><body>
                <b>{#include template /}</b> - Include Template<br/>
                <p>Includes another template file</p>
                <p>Example: <code>{#include partials/header /}</code></p>
                </body></html>
            """.trimIndent()

            "insert" -> """
                <html><body>
                <b>{#insert}</b> - Content Insertion Point<br/>
                <p>Defines where child content will be inserted in layouts</p>
                <p>Used in layout files to mark content insertion points</p>
                </body></html>
            """.trimIndent()

            else -> """
                <html><body>
                <b>{#$directive}</b> - Qute Directive<br/>
                <p>Qute template directive</p>
                <p>See Qute documentation for details</p>
                </body></html>
            """.trimIndent()
        }
    }
}
