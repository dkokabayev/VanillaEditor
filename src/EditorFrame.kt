import controls.text.TextArea
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class EditorFrame : JFrame() {

    private companion object EditorSettings {
        const val FONT_NAME = "Monospaced"
        const val FONT_SIZE = 13
        const val EDITOR_WIDTH = 800
        const val EDITOR_HEIGHT = 600
        const val FRAME_TITLE = "Vanilla Editor"
        const val FILE_MENU_TITLE = "File"
        const val OPEN_FILE_MENU_ITEM_TITLE = "Open"
        const val SAVE_FILE_MENU_ITEM_TITLE = "Save"
        const val FILE_FILTER_DESCRIPTION = "java files"
        const val FILE_EXTENSION = "java"
        const val VIEW_MENU_TITLE = "View"
        const val SHOW_LINE_NUMBERS_MENU_ITEM_TITLE = "Show Line Numbers"
        const val COLOR_SETTINGS_MENU_ITEM_TITLE = "Color Settings"
    }

    private val textArea: TextArea = TextArea(
        fontName = FONT_NAME,
        fontSize = FONT_SIZE,
    )

    init {
        this.title = FRAME_TITLE

        this.defaultCloseOperation = EXIT_ON_CLOSE
        this.setSize(EDITOR_WIDTH, EDITOR_HEIGHT)

        val menuBar = JMenuBar()
        menuBar.add(createFileMenu())
        menuBar.add(createViewMenu())
        jMenuBar = menuBar

        contentPane.add(textArea)
    }

    private fun createFileMenu(): JMenu {
        val fileMenu = JMenu(FILE_MENU_TITLE)

        val openItem = JMenuItem(OPEN_FILE_MENU_ITEM_TITLE).apply {
            addActionListener {
                val chooser = JFileChooser()
                chooser.fileFilter = FileNameExtensionFilter(FILE_FILTER_DESCRIPTION, FILE_EXTENSION)
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    textArea.text = chooser.selectedFile.readText()
                }
            }
        }

        val saveItem = JMenuItem(SAVE_FILE_MENU_ITEM_TITLE).apply {
            addActionListener {
                val chooser = JFileChooser()
                chooser.fileFilter = FileNameExtensionFilter(FILE_FILTER_DESCRIPTION, FILE_EXTENSION)
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    chooser.selectedFile.writeText(textArea.text)
                }
            }
        }

        fileMenu.add(openItem)
        fileMenu.add(saveItem)
        return fileMenu
    }

    private fun createViewMenu(): JMenu {
        val viewMenu = JMenu(VIEW_MENU_TITLE)

        val showLineNumbersMenuItem = JCheckBoxMenuItem(SHOW_LINE_NUMBERS_MENU_ITEM_TITLE).apply {
            isSelected = textArea.lineNumbersVisible
            addActionListener { textArea.lineNumbersVisible = isSelected }
        }

        viewMenu.add(showLineNumbersMenuItem)

        val colorSettingsItem = JMenuItem(COLOR_SETTINGS_MENU_ITEM_TITLE).apply {
            addActionListener {
                ColorSettingsDialog(textArea).isVisible = true
            }
        }

        viewMenu.addSeparator()
        viewMenu.add(colorSettingsItem)

        return viewMenu
    }
}