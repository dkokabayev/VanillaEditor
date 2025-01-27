import controls.text.TextComponent
import java.awt.BorderLayout
import javax.swing.*

object EditorSettings {
    const val FONT_NAME = "Monospaced"
    const val FONT_SIZE = 14
    const val CARET_BLINK_RATE = 500
    const val BACKSPACE_REPEAT_RATE = 50
    const val EDITOR_WIDTH = 800
    const val EDITOR_HEIGHT = 600
    const val FRAME_TITLE = "Vanilla Editor"
}

fun main() {
    SwingUtilities.invokeLater {
        val frame = JFrame(EditorSettings.FRAME_TITLE)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(EditorSettings.EDITOR_WIDTH, EditorSettings.EDITOR_HEIGHT)

        val textComponent = TextComponent(
            fontName = EditorSettings.FONT_NAME,
            fontSize = EditorSettings.FONT_SIZE,
            caretBlinkRate = EditorSettings.CARET_BLINK_RATE,
            backspaceRepeatRate = EditorSettings.BACKSPACE_REPEAT_RATE,
        )

        frame.add(JScrollPane(textComponent), BorderLayout.CENTER)
        frame.isVisible = true
    }
}