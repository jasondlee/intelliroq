package com.steeplesoft.intelliroq.lang

import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class RoqIncludeReferenceTest : BasePlatformTestCase() {

    fun testResolveIncludeToPartialFile() {
        // Create the partial file that will be referenced
        val partialFile = myFixture.addFileToProject(
            "templates/partials/header.html",
            "<header>Site Header</header>"
        )

        // Create a file with an include statement
        val includeFile = myFixture.addFileToProject(
            "index.html",
            """
            <html>
            <body>
                {#include partials/header /}
            </body>
            </html>
            """.trimIndent()
        )

        // Get all references in the file
        val provider = RoqIncludeReferenceProvider()
        val references = provider.getReferencesByElement(includeFile, com.intellij.util.ProcessingContext())

        // Verify we found a reference
        assertTrue("Should find at least one reference", references.isNotEmpty())

        val reference = references.firstOrNull { it is RoqIncludeReference } as? RoqIncludeReference
        assertNotNull("Should find a RoqIncludeReference", reference)

        // Verify the reference resolves to the partial file
        val resolved = reference?.resolve()
        assertNotNull("Reference should resolve", resolved)
        assertEquals("Should resolve to the partial file", partialFile, resolved)
    }

    fun testResolveIncludeWithoutExtension() {
        // Create the partial file
        val partialFile = myFixture.addFileToProject(
            "templates/partials/footer.html",
            "<footer>Footer content</footer>"
        )

        // Test include without .html extension
        val includeFile = myFixture.addFileToProject(
            "page.md",
            """
            # My Page

            {#include partials/footer /}
            """.trimIndent()
        )

        val provider = RoqIncludeReferenceProvider()
        val references = provider.getReferencesByElement(includeFile, com.intellij.util.ProcessingContext())

        val reference = references.firstOrNull() as? RoqIncludeReference
        assertNotNull("Reference should be found for include without extension", reference)

        val resolved = reference?.resolve()
        assertNotNull("Should resolve even without .html extension", resolved)
        assertEquals("Should resolve to the footer partial", partialFile, resolved)
    }

    fun testResolveIncludeWithExtension() {
        // Create the partial file
        val partialFile = myFixture.addFileToProject(
            "templates/partials/nav.html",
            "<nav>Navigation</nav>"
        )

        // Test include with .html extension
        val includeFile = myFixture.addFileToProject(
            "page.adoc",
            """
            = My Page

            {#include partials/nav.html /}
            """.trimIndent()
        )

        val provider = RoqIncludeReferenceProvider()
        val references = provider.getReferencesByElement(includeFile, com.intellij.util.ProcessingContext())

        val reference = references.firstOrNull() as? RoqIncludeReference
        assertNotNull("Reference should be found for include with extension", reference)

        val resolved = reference?.resolve()
        assertNotNull("Should resolve with .html extension", resolved)
        assertEquals("Should resolve to the nav partial", partialFile, resolved)
    }

    fun testMultipleIncludesInSameFile() {
        // Create multiple partials
        val headerFile = myFixture.addFileToProject("templates/partials/header.html", "<header/>")
        val footerFile = myFixture.addFileToProject("templates/partials/footer.html", "<footer/>")
        val mainFile = myFixture.addFileToProject("templates/layouts/main.html", "<main/>")

        val includeFile = myFixture.addFileToProject(
            "page.html",
            """
            {#include partials/header /}
            <div>Content</div>
            {#include partials/footer /}
            {#include layouts/main /}
            """.trimIndent()
        )

        val provider = RoqIncludeReferenceProvider()
        val references = provider.getReferencesByElement(includeFile, com.intellij.util.ProcessingContext())

        // Should find 3 references (one for each include)
        assertEquals("Should find 3 references", 3, references.size)

        // Verify each resolves correctly
        val headerRef = references.find { it.canonicalText == "partials/header" }
        assertNotNull("Should find header reference", headerRef)
        assertEquals("Header should resolve correctly", headerFile, headerRef?.resolve())

        val footerRef = references.find { it.canonicalText == "partials/footer" }
        assertNotNull("Should find footer reference", footerRef)
        assertEquals("Footer should resolve correctly", footerFile, footerRef?.resolve())

        val mainRef = references.find { it.canonicalText == "layouts/main" }
        assertNotNull("Should find main reference", mainRef)
        assertEquals("Main should resolve correctly", mainFile, mainRef?.resolve())
    }

    fun testIncludeInNestedElement() {
        // Create the partial file
        val partialFile = myFixture.addFileToProject(
            "templates/partials/sidebar.html",
            "<aside>Sidebar</aside>"
        )

        val includeFile = myFixture.addFileToProject(
            "nested.html",
            """
            <html>
            <body>
                <div class="container">
                    <div class="row">
                        {#include partials/sidebar /}
                    </div>
                </div>
            </body>
            </html>
            """.trimIndent()
        )

        // Check the XmlTextImpl element which contains the include
        fun findXmlTextElements(element: com.intellij.psi.PsiElement): List<com.intellij.psi.PsiElement> {
            val result = mutableListOf<com.intellij.psi.PsiElement>()
            if (element.javaClass.simpleName == "XmlTextImpl") {
                result.add(element)
            }
            element.children.forEach { result.addAll(findXmlTextElements(it)) }
            return result
        }

        val xmlTextElements = findXmlTextElements(includeFile)
        val textWithInclude = xmlTextElements.find { it.text.contains("{#include") }
        assertNotNull("Should find XmlTextImpl with include", textWithInclude)

        val provider = RoqIncludeReferenceProvider()
        val references = provider.getReferencesByElement(textWithInclude!!, com.intellij.util.ProcessingContext())

        assertTrue("Should find reference in nested element", references.isNotEmpty())
        val resolved = references.first().resolve()
        assertEquals("Should resolve to the sidebar partial", partialFile, resolved)
    }
}
