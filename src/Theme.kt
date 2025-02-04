import com.formdev.flatlaf.themes.FlatMacDarkLaf
import com.formdev.flatlaf.themes.FlatMacLightLaf
import java.awt.Color

sealed class Theme(
    val uiManager: javax.swing.LookAndFeel,
    val colors: ThemeColors
) {
    data object Dark : Theme(
        uiManager = FlatMacDarkLaf(),
        colors = ThemeColors(
            background = Color(36, 36, 36),
            foreground = Color(235, 235, 235),
            selection = Color(34, 84, 170),
            scrollBar = Color(88, 88, 88),
            scrollBarHover = Color(102, 102, 102),
            scrollBarDrag = Color(125, 125, 125),
            scrollBarBackground = Color(50, 50, 50),
            caret = Color(235, 235, 235),
            lineNumbersText = Color(153, 153, 153),
            lineNumbersBackground = Color(36, 36, 36)
        )
    )

    data object Light : Theme(
        uiManager = FlatMacLightLaf(),
        colors = ThemeColors(
            background = Color(255, 255, 255),
            foreground = Color(0, 0, 0),
            selection = Color(168, 206, 255),
            scrollBar = Color(177, 177, 177),
            scrollBarHover = Color(164, 164, 164),
            scrollBarDrag = Color(139, 139, 139),
            scrollBarBackground = Color(238, 238, 238),
            caret = Color(0, 0, 0),
            lineNumbersText = Color(122, 122, 122),
            lineNumbersBackground = Color(255, 255, 255)
        )
    )
}
