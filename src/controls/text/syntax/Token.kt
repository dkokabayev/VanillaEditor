package controls.text.syntax

/**
 * Represents a token in the syntax highlighting system.
 * Each token has a type and associated text content.
 *
 * @property type The type of the token
 * @property text The actual text content of the token
 */
sealed class Token(val type: TokenType, val text: kotlin.String) {
    class Keyword(text: kotlin.String) : Token(TokenType.KEYWORD, text)
    class String(text: kotlin.String) : Token(TokenType.STRING, text)
    class Number(text: kotlin.String) : Token(TokenType.NUMBER, text)
    class Comment(text: kotlin.String) : Token(TokenType.COMMENT, text)
    class Annotation(text: kotlin.String) : Token(TokenType.ANNOTATION, text)
    class Type(text: kotlin.String) : Token(TokenType.TYPE, text)
    class Method(text: kotlin.String) : Token(TokenType.METHOD, text)
    class Operator(text: kotlin.String) : Token(TokenType.OPERATOR, text)
    class Plain(text: kotlin.String) : Token(TokenType.PLAIN, text)
}