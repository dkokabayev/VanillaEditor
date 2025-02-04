package controls.text.syntax

import java.awt.Color

/**
 * Interface defining colors for different syntax highlighting elements.
 * Implementations provide specific color schemes for syntax highlighting.
 */
interface SyntaxThemeColors {
    /** Color for language keywords */
    val keyword: Color

    /** Color for string literals */
    val string: Color

    /** Color for numeric literals */
    val number: Color

    /** Color for comments */
    val comment: Color

    /** Color for annotations */
    val annotation: Color

    /** Color for type names */
    val type: Color

    /** Color for method names */
    val method: Color

    /** Color for operators */
    val operator: Color

    /** Color for plain text */
    val plain: Color
}

/**
 * Implementation of SyntaxThemeColors providing a light color scheme for syntax highlighting.
 */
class LightThemeColors : SyntaxThemeColors {
    override val keyword = Color(204, 120, 50)
    override val string = Color(98, 151, 85)
    override val number = Color(104, 151, 187)
    override val comment = Color(128, 128, 128)
    override val annotation = Color(191, 139, 38)
    override val type = Color(178, 178, 178)
    override val method = Color(255, 198, 109)
    override val operator = Color(204, 120, 50)
    override val plain = Color(0, 0, 0)
}

/**
 * Implementation of SyntaxThemeColors providing a dark color scheme for syntax highlighting.
 */
class DarkThemeColors : SyntaxThemeColors {
    override val keyword = Color(255, 128, 64)
    override val string = Color(106, 168, 79)
    override val number = Color(104, 151, 187)
    override val comment = Color(128, 128, 128)
    override val annotation = Color(255, 198, 109)
    override val type = Color(169, 183, 198)
    override val method = Color(255, 198, 109)
    override val operator = Color(255, 128, 64)
    override val plain = Color(235, 235, 235)
}

