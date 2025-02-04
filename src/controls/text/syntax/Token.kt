package controls.text.syntax


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