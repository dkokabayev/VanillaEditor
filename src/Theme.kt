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
            background = Color(43, 43, 43),
            foreground = Color(187, 187, 187),
            selection = Color(33, 66, 131),
            scrollBar = Color(85, 85, 85),
            scrollBarHover = Color(100, 100, 100),
            scrollBarDrag = Color(110, 110, 110),
            scrollBarBackground = Color(49, 49, 49),
            caret = Color(187, 187, 187),
            lineNumbersText = Color(145, 145, 145),
            lineNumbersBackground = Color(43, 43, 43)
        )
    )

    data object Light : Theme(
        uiManager = FlatLightLaf(),
        colors = ThemeColors(
            background = Color(250, 250, 250),
            foreground = Color(30, 30, 30),
            selection = Color(164, 191, 236),
            scrollBar = Color(205, 205, 205),
            scrollBarHover = Color(190, 190, 190),
            scrollBarDrag = Color(172, 172, 172),
            scrollBarBackground = Color(245, 245, 245),
            caret = Color(30, 30, 30),
            lineNumbersText = Color(110, 110, 110),
            lineNumbersBackground = Color(250, 250, 250)
        )
    )
}
