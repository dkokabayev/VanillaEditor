package syntax

import controls.text.syntax.SyntaxHighlighter

object SyntaxHighlighterFactory {
    fun createHighlighter(fileName: String): SyntaxHighlighter? {
        return when {
            fileName.endsWith(".java") -> JavaHighlighter()
            fileName.endsWith(".kt") -> KotlinHighlighter()
            else -> null
        }
    }
}