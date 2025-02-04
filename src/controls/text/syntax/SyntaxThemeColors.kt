package controls.text.syntax

import java.awt.Color

interface SyntaxThemeColors {
    val keyword: Color
    val string: Color
    val number: Color
    val comment: Color
    val annotation: Color
    val type: Color
    val method: Color
    val operator: Color
    val plain: Color
}

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

