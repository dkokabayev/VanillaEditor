package controls.text.syntax

interface SyntaxHighlighter {
    fun highlight(text: String): List<Token>
}