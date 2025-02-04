package controls.text.syntax

/**
 * Interface for implementing syntax highlighting functionality.
 * Implementations should process text and return a list of tokens with their corresponding types.
 */
interface SyntaxHighlighter {
    fun highlight(text: String): List<Token>
}