import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import java.awt.Color

sealed class Theme(
    val uiManager: javax.swing.LookAndFeel,
    val colors: ThemeColors
) {
    data object Dark : Theme(
        uiManager = FlatDarkLaf(),
        colors = ThemeColors(
            background = Color(50, 50, 50),
            foreground = Color(220, 220, 220),
            selection = Color(100, 100, 100),
            scrollBar = Color(80, 80, 80),
            scrollBarHover = Color(100, 100, 100),
            scrollBarDrag = Color(120, 120, 120),
            scrollBarBackground = Color(60, 60, 60),
            caret = Color.WHITE,
            lineNumbersText = Color(150, 150, 150),
            lineNumbersBackground = Color(45, 45, 45)
        )
    )

    data object Light : Theme(
        uiManager = FlatLightLaf(),
        colors = ThemeColors(
            background = Color(230, 230, 230),
            foreground = Color.BLACK,
            selection = Color.PINK,
            scrollBar = Color.LIGHT_GRAY,
            scrollBarHover = Color.GRAY,
            scrollBarDrag = Color(110, 110, 110),
            scrollBarBackground = Color(230, 230, 230),
            caret = Color.BLACK,
            lineNumbersText = Color.GRAY,
            lineNumbersBackground = Color(230, 230, 230)
        )
    )
}

data class ThemeColors(
    val background: Color,
    val foreground: Color,
    val selection: Color,
    val scrollBar: Color,
    val scrollBarHover: Color,
    val scrollBarDrag: Color,
    val scrollBarBackground: Color,
    val caret: Color,
    val lineNumbersText: Color,
    val lineNumbersBackground: Color
)