package syntax

import controls.text.syntax.SyntaxHighlighter
import controls.text.syntax.Token

class KotlinHighlighter : SyntaxHighlighter {
    private companion object {
        private val KEYWORDS = setOf(
            "package", "import", "class", "interface", "fun", "val", "var", "if", "else",
            "when", "while", "for", "do", "try", "catch", "finally", "throw", "return",
            "continue", "break", "as", "is", "in", "!in", "object", "companion", "init",
            "constructor", "data", "sealed", "private", "protected", "public", "internal",
            "override", "abstract", "final", "open", "enum", "annotation", "suspend",
            "tailrec", "operator", "infix", "inline", "external", "vararg"
        )

        private val OPERATORS = setOf(
            "+", "-", "*", "/", "%", "=", "+=", "-=", "*=", "/=", "%=",
            "==", "!=", ">", "<", ">=", "<=", "&&", "||", "!", "?:", "?.",
            "..", "->", "::", "?", "!!", "to", "..", "until", "step"
        )

        private val TYPE_KEYWORDS = setOf(
            "Int", "Long", "Float", "Double", "Boolean", "Char", "String",
            "Array", "List", "Set", "Map", "Any", "Unit", "Nothing"
        )
    }

    override fun highlight(text: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var current = 0

        while (current < text.length) {
            when {
                text.startsWith("//", current) -> {
                    val end = text.indexOf('\n', current).takeIf { it != -1 } ?: text.length
                    tokens.add(Token.Comment(text.substring(current, end)))
                    current = end
                }

                text.startsWith("/*", current) -> {
                    val end = text.indexOf("*/", current).takeIf { it != -1 }?.plus(2) ?: text.length
                    tokens.add(Token.Comment(text.substring(current, end)))
                    current = end
                }

                text.startsWith("\"", current) -> {
                    var end = current + 1
                    while (end < text.length && text[end] != '\"' && text[end - 1] != '\\') end++
                    if (end < text.length) end++
                    tokens.add(Token.String(text.substring(current, end)))
                    current = end
                }

                text.startsWith("@", current) -> {
                    var end = current + 1
                    while (end < text.length && (text[end].isLetterOrDigit() || text[end] == '_')) end++
                    tokens.add(Token.Annotation(text.substring(current, end)))
                    current = end
                }

                text[current].isLetter() -> {
                    var end = current + 1
                    while (end < text.length && (text[end].isLetterOrDigit() || text[end] == '_')) end++
                    val word = text.substring(current, end)
                    tokens.add(
                        when {
                            KEYWORDS.contains(word) -> Token.Keyword(word)
                            TYPE_KEYWORDS.contains(word) -> Token.Type(word)
                            else -> Token.Plain(word)
                        }
                    )
                    current = end
                }

                text[current].isDigit() -> {
                    var end = current + 1
                    while (end < text.length && (text[end].isDigit() || text[end] == '.')) end++
                    tokens.add(Token.Number(text.substring(current, end)))
                    current = end
                }

                else -> {
                    val operator = OPERATORS.find { text.startsWith(it, current) }
                    if (operator != null) {
                        tokens.add(Token.Operator(operator))
                        current += operator.length
                    } else {
                        tokens.add(Token.Plain(text[current].toString()))
                        current++
                    }
                }
            }
        }

        return tokens
    }
}