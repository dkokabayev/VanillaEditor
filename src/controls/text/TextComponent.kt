package controls.text

import java.awt.*
import java.awt.event.*
import java.io.File
import javax.swing.JComponent
import javax.swing.Timer

class TextComponent(
    fontName: String = "Monospaced",
    fontSize: Int = 14,
    caretBlinkRate: Int = 500,
    private val backspaceRepeatRate: Int = 50,
    private val newLineChar: Char = '\n',
) : JComponent() {

    private val textBuffer = TextBuffer(newLineChar)
    private val caretModel = CaretModel(textBuffer)
    private var caretVisible = true

    private var backspacePressed = false
    private var backspaceTimer: Timer? = null

    init {
        font = Font(fontName, Font.PLAIN, fontSize)
        addKeyListener(TextKeyListener())
        addMouseListener(TextMouseListener())
        isFocusable = true
        focusTraversalKeysEnabled = false

        Timer(caretBlinkRate) {
            caretVisible = !caretVisible
            repaint()
        }.start()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.font = font

        val fm = g.fontMetrics
        val lineHeight = fm.ascent + fm.descent

        var y = lineHeight
        val lines = textBuffer.getLines()
        for (line in lines) {
            g.drawString(line, 5, y)
            y += lineHeight
        }

        if (caretVisible) {
            val (caretX, caretY) = getCaretCoordinates(fm)
            g.drawLine(caretX + 5, caretY - fm.ascent, caretX + 5, caretY - fm.ascent + lineHeight)
        }
    }

    private fun getCaretCoordinates(fm: FontMetrics): Pair<Int, Int> {
        val caretPosition = caretModel.getCurrentPosition()
        val textBeforeCaret = textBuffer.getText().substring(0, caretPosition.offset)
        val splitBeforeCaret = textBeforeCaret.split(newLineChar)

        val caretLineIndex = splitBeforeCaret.size - 1
        val currentLineText = splitBeforeCaret.lastOrNull() ?: ""

        val x = fm.stringWidth(currentLineText)
        val y = (fm.ascent + fm.descent) * (caretLineIndex + 1)
        return Pair(x, y)
    }

    private inner class TextKeyListener : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_BACK_SPACE -> {
                    if (!backspacePressed) {
                        backspacePressed = true
                        val position = caretModel.getCurrentPosition()
                        if (position.offset > 0) {
                            textBuffer.deleteCharAt(position.offset - 1)
                            caretModel.moveLeft()
                        }
                        startBackspaceTimer()
                    }
                }

                KeyEvent.VK_DELETE -> {
                    val position = caretModel.getCurrentPosition()
                    if (position.offset < textBuffer.length) {
                        textBuffer.deleteCharAt(position.offset)
                    }
                }

                KeyEvent.VK_LEFT -> {
                    when {
                        e.isControlDown || e.isMetaDown -> caretModel.moveToLineStart()
                        e.isAltDown -> caretModel.moveToPreviousWord()
                        else -> caretModel.moveLeft()
                    }
                }

                KeyEvent.VK_RIGHT -> {
                    when {
                        e.isControlDown || e.isMetaDown -> caretModel.moveToLineEnd()
                        e.isAltDown -> caretModel.moveToNextWord()
                        else -> caretModel.moveRight()
                    }
                }

                KeyEvent.VK_UP -> {
                    when {
                        e.isControlDown || e.isMetaDown -> caretModel.moveToTextStart()
                        e.isAltDown -> caretModel.moveUpWithOption()
                    }
                }

                KeyEvent.VK_DOWN -> {
                    when {
                        e.isControlDown || e.isMetaDown -> caretModel.moveToTextEnd()
                        e.isAltDown -> caretModel.moveDownWithOption()
                    }
                }

                KeyEvent.VK_ENTER -> {
                    val position = caretModel.getCurrentPosition()
                    textBuffer.insertChar(newLineChar, position.offset)
                    caretModel.moveRight()
                }
            }
            repaint()
        }

        override fun keyReleased(e: KeyEvent) {
            if (e.keyCode == KeyEvent.VK_BACK_SPACE) {
                backspacePressed = false
                backspaceTimer?.stop()
            }
        }

        override fun keyTyped(e: KeyEvent) {
            if (e.keyChar != KeyEvent.CHAR_UNDEFINED && !e.isControlDown && e.keyChar != newLineChar) {
                val position = caretModel.getCurrentPosition()
                textBuffer.insertChar(e.keyChar, position.offset)
                caretModel.moveRight()
                repaint()
            }
        }

        private fun startBackspaceTimer() {
            backspaceTimer = Timer(backspaceRepeatRate) {
                if (backspacePressed) {
                    val position = caretModel.getCurrentPosition()
                    if (position.offset > 0) {
                        textBuffer.deleteCharAt(position.offset - 1)
                        caretModel.moveLeft()
                        repaint()
                    }
                }
            }.apply { start() }
        }
    }

    private inner class TextMouseListener : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            requestFocusInWindow()
        }
    }

    fun openFile(file: File) {
        textBuffer.loadFromFile(file)
        caretModel.moveToTextEnd()
        repaint()
    }

    fun saveFile(file: File) {
        textBuffer.saveToFile(file)
    }
}