import controls.text.TextComponent
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

object EditorSettings {
    val FONT_COLOR: Color = Color.BLACK
    val SELECTION_COLOR: Color = Color.PINK
    const val FONT_NAME = "Monospaced"
    const val FONT_SIZE = 14
    const val CARET_BLINK_RATE = 500
    const val BACKSPACE_REPEAT_RATE = 50
    const val EDITOR_WIDTH = 800
    const val EDITOR_HEIGHT = 600
    const val FRAME_TITLE = "Vanilla Editor"
    const val FILE_MENU_TITLE = "File"
    const val OPEN_FILE_TITLE = "Open"
    const val SAVE_FILE_TITLE = "Save"
    const val FILE_FILTER_DESCRIPTION = "java files"
    const val FILE_EXTENSION = "java"
    const val NEW_LINE_CHAR = '\n'
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
            newLineChar = EditorSettings.NEW_LINE_CHAR,
            fontColor = EditorSettings.FONT_COLOR,
            selectionColor = EditorSettings.SELECTION_COLOR,
        )

        frame.add(JScrollPane(textComponent), BorderLayout.CENTER)

        val menuBar = JMenuBar()
        val fileMenu = JMenu(EditorSettings.FILE_MENU_TITLE)

        val openItem = JMenuItem(EditorSettings.OPEN_FILE_TITLE).apply {
            addActionListener {
                val chooser = JFileChooser()
                chooser.fileFilter =
                    FileNameExtensionFilter(EditorSettings.FILE_FILTER_DESCRIPTION, EditorSettings.FILE_EXTENSION)
                if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    textComponent.openFile(chooser.selectedFile)
                }
            }
        }

        val saveItem = JMenuItem(EditorSettings.SAVE_FILE_TITLE).apply {
            addActionListener {
                val chooser = JFileChooser()
                chooser.fileFilter =
                    FileNameExtensionFilter(EditorSettings.FILE_FILTER_DESCRIPTION, EditorSettings.FILE_EXTENSION)
                if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    textComponent.saveFile(chooser.selectedFile)
                }
            }
        }

        fileMenu.add(openItem)
        fileMenu.add(saveItem)
        menuBar.add(fileMenu)
        frame.jMenuBar = menuBar

        frame.isVisible = true
    }
}