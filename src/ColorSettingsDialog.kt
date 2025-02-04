import controls.text.TextArea
import java.awt.*
import javax.swing.*

class ColorSettingsDialog(
    private val textArea: TextArea
) : JDialog(SwingUtilities.getWindowAncestor(textArea), TITLE, ModalityType.APPLICATION_MODAL) {

    private companion object {
        const val TITLE = "Color Settings"
        const val TEXT_COLOR = "Text Color"
        const val SELECTION_COLOR = "Selection Color"
        const val CARET_COLOR = "Caret Color"
        const val SCROLLBAR_COLOR = "ScrollBar Color"
        const val SCROLLBAR_HOVER = "ScrollBar Hover"
        const val SCROLLBAR_DRAG = "ScrollBar Drag"
        const val SCROLLBAR_BACKGROUND = "ScrollBar Background"
        const val LINE_NUMBERS_COLOR = "Line Numbers Color"
        const val LINE_NUMBERS_BACKGROUND = "Line Numbers Background"
    }

    private class ColorButton(initialColor: Color) : JButton() {
        init {
            preferredSize = Dimension(100, 25)

            object : JComponent() {
                init {
                    preferredSize = Dimension(80, 20)
                    background = initialColor
                    isOpaque = true
                }

                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    g.color = background
                    g.fillRect(0, 0, width, height)
                    g.color = Color.BLACK
                    g.drawRect(0, 0, width - 1, height - 1)
                }
            }.also { add(it) }
        }

        var currentColor: Color
            get() = background
            set(value) {
                background = value
                components.forEach { it.background = value }
                repaint()
            }
    }

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        createUI()
        pack()
        setLocationRelativeTo(owner)
    }

    private fun createUI() {
        val panel = JPanel(GridLayout(0, 2, 5, 2)).apply {
            border = BorderFactory.createEmptyBorder(8, 12, 8, 12)
        }

        fun addColorControl(name: String, initialColor: Color, onColorChange: (Color) -> Unit) {
            panel.add(JLabel(name))
            panel.add(ColorButton(initialColor).apply {
                addActionListener {
                    val newColor = JColorChooser.showDialog(this@ColorSettingsDialog, "Choose $name", currentColor)
                    if (newColor != null) {
                        currentColor = newColor
                        onColorChange(newColor)
                    }
                }
            })
        }

        addColorControl(TEXT_COLOR, textArea.foregroundColor) {
            textArea.foregroundColor = it
        }
        addColorControl(SELECTION_COLOR, textArea.selectionColor) {
            textArea.selectionColor = it
        }
        addColorControl(CARET_COLOR, textArea.caretColor) {
            textArea.caretColor = it
        }
        addColorControl(SCROLLBAR_COLOR, textArea.scrollBarColor) {
            textArea.scrollBarColor = it
        }
        addColorControl(SCROLLBAR_HOVER, textArea.scrollBarHoverColor) {
            textArea.scrollBarHoverColor = it
        }
        addColorControl(SCROLLBAR_DRAG, textArea.scrollBarDragColor) {
            textArea.scrollBarDragColor = it
        }
        addColorControl(SCROLLBAR_BACKGROUND, textArea.scrollBarBackgroundColor) {
            textArea.scrollBarBackgroundColor = it
        }
        addColorControl(LINE_NUMBERS_COLOR, textArea.lineNumbersColumnColor) {
            textArea.lineNumbersColumnColor = it
        }
        addColorControl(LINE_NUMBERS_BACKGROUND, textArea.lineNumbersColumnBackgroundColor) {
            textArea.lineNumbersColumnBackgroundColor = it
        }

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 5)).apply {
            add(JButton("Close").apply {
                addActionListener { dispose() }
            })
        }

        contentPane.apply {
            layout = BorderLayout(0, 5)
            add(panel, BorderLayout.CENTER)
            add(buttonPanel, BorderLayout.SOUTH)
        }
    }
}