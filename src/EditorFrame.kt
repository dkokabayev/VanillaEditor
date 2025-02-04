import controls.text.TextArea
import java.awt.Color
import java.awt.event.KeyEvent
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
        const val FILE_FILTER_DESCRIPTION = "Java, Kotlin, and Text Files"
        val FILE_EXTENSIONS = arrayOf("java", "kt", "txt")
        const val VIEW_MENU_TITLE = "View"
        const val SHOW_LINE_NUMBERS_MENU_ITEM_TITLE = "Show Line Numbers"
        const val COLOR_SETTINGS_MENU_ITEM_TITLE = "Color Settings"
        const val THEMES_MENU_TITLE = "Themes"
        const val DARK_THEME_MENU_ITEM_TITLE = "Dark Theme"
        const val LIGHT_THEME_MENU_ITEM_TITLE = "Light Theme"
        val COMMAND_OR_CTRL_MASK = if (System.getProperty("os.name").lowercase().contains("mac"))
            KeyEvent.META_DOWN_MASK else KeyEvent.CTRL_DOWN_MASK
    }

    private val textArea: TextArea = TextArea(
        fontName = FONT_NAME,
        fontSize = FONT_SIZE,
    )

    private var currentTheme: Theme = Theme.Dark
        set(value) {
            field = value
            applyTheme()
        }

    private fun applyTheme() {
        UIManager.setLookAndFeel(currentTheme.uiManager)
        SwingUtilities.updateComponentTreeUI(this)

        with(currentTheme.colors) {
            textArea.apply {
                background = this@with.background
                foreground = this@with.foreground
                foregroundColor = foreground
                selectionColor = selection
                scrollBarColor = scrollBar
                scrollBarHoverColor = scrollBarHover
                scrollBarDragColor = scrollBarDrag
                scrollBarBackgroundColor = scrollBarBackground
                caretColor = caret
                lineNumbersColumnColor = lineNumbersText
                lineNumbersColumnBackgroundColor = lineNumbersBackground
            }
        }
    }

    init {
        this.title = FRAME_TITLE

        this.defaultCloseOperation = EXIT_ON_CLOSE
        this.setSize(EDITOR_WIDTH, EDITOR_HEIGHT)

        val menuBar = JMenuBar()
        menuBar.add(createFileMenu())
        menuBar.add(createViewMenu())
        jMenuBar = menuBar

        contentPane.add(textArea)

        applyTheme()
    }

    private fun createFileMenu(): JMenu {
        val fileMenu = JMenu(FILE_MENU_TITLE).apply {
            mnemonic = KeyEvent.VK_F
        }
        fileMenu.background = Color.BLACK

        val openItem = JMenuItem(OPEN_FILE_MENU_ITEM_TITLE).apply {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_O, COMMAND_OR_CTRL_MASK)
            mnemonic = KeyEvent.VK_O
            addActionListener {
                val chooser = JFileChooser()
                chooser.fileFilter = FileNameExtensionFilter(FILE_FILTER_DESCRIPTION, *FILE_EXTENSIONS)
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    textArea.text = chooser.selectedFile.readText()
                }
            }
        }

        val saveItem = JMenuItem(SAVE_FILE_MENU_ITEM_TITLE).apply {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, COMMAND_OR_CTRL_MASK)
            mnemonic = KeyEvent.VK_S
            addActionListener {
                val chooser = JFileChooser()
                chooser.fileFilter = FileNameExtensionFilter(FILE_FILTER_DESCRIPTION, *FILE_EXTENSIONS)
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
        val viewMenu = JMenu(VIEW_MENU_TITLE).apply {
            mnemonic = KeyEvent.VK_V
        }

        val showLineNumbersMenuItem = JCheckBoxMenuItem(SHOW_LINE_NUMBERS_MENU_ITEM_TITLE).apply {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_L, COMMAND_OR_CTRL_MASK)
            mnemonic = KeyEvent.VK_L
            isSelected = textArea.lineNumbersVisible
            addActionListener { textArea.lineNumbersVisible = isSelected }
        }

        viewMenu.add(showLineNumbersMenuItem)

        val colorSettingsItem = JMenuItem(COLOR_SETTINGS_MENU_ITEM_TITLE).apply {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_K, COMMAND_OR_CTRL_MASK)
            mnemonic = KeyEvent.VK_C
            addActionListener {
                ColorSettingsDialog(textArea).isVisible = true
            }
        }

        viewMenu.addSeparator()
        viewMenu.add(colorSettingsItem)

        val themesMenu = JMenu(THEMES_MENU_TITLE).apply {
            mnemonic = KeyEvent.VK_T
        }

        val darkThemeItem = JRadioButtonMenuItem(DARK_THEME_MENU_ITEM_TITLE, true).apply {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_D, COMMAND_OR_CTRL_MASK or KeyEvent.SHIFT_DOWN_MASK)
            mnemonic = KeyEvent.VK_D
        }

        val lightThemeItem = JRadioButtonMenuItem(LIGHT_THEME_MENU_ITEM_TITLE, false).apply {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_L, COMMAND_OR_CTRL_MASK or KeyEvent.SHIFT_DOWN_MASK)
            mnemonic = KeyEvent.VK_L
        }

        val themeGroup = ButtonGroup()
        themeGroup.add(darkThemeItem)
        themeGroup.add(lightThemeItem)

        darkThemeItem.addActionListener { currentTheme = Theme.Dark }
        lightThemeItem.addActionListener { currentTheme = Theme.Light }

        themesMenu.add(darkThemeItem)
        themesMenu.add(lightThemeItem)

        viewMenu.add(themesMenu)

        return viewMenu
    }
}