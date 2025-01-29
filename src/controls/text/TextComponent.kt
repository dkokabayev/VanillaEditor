package controls.text

import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JComponent
import javax.swing.Timer

class TextComponent(
    private val fontName: String = "Monospaced",
    private val fontSize: Int = 14,
    private val caretBlinkRate: Int = 500,
    private val backspaceRepeatRate: Int = 50,
    private val newLineChar: Char = '\n',
) : JComponent() {

    private val textBuffer = TextBuffer(newLineChar)
    private var caretVisible = true
    private val defaultFont = Font(fontName, Font.PLAIN, fontSize)

    private var backspacePressed = false
    private var backspaceTimer: Timer? = null

    init {
        font = defaultFont
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
        g.font = defaultFont

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
        val textBeforeCaret = textBuffer.getLines().joinToString("\n").substring(0, textBuffer.caretPosition)
        val splitBeforeCaret = textBeforeCaret.split("\n")

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
                        textBuffer.deleteBeforeCaret()
                        startBackspaceTimer()
                    }
                }

                KeyEvent.VK_DELETE -> textBuffer.deleteAtCaret()
                KeyEvent.VK_LEFT -> {
                    when {
                        e.isAltDown -> textBuffer.moveCaretToPreviousWord()
                        else -> textBuffer.moveCaretLeft()
                    }
                }

                KeyEvent.VK_RIGHT -> {
                    when {
                        e.isAltDown -> textBuffer.moveCaretToNextWord()
                        else -> textBuffer.moveCaretRight()
                    }
                }

                KeyEvent.VK_UP -> {
                    if (e.isAltDown) {
                        textBuffer.moveCaretUpWithOption()
                    }
                }

                KeyEvent.VK_DOWN -> {
                    if (e.isAltDown) {
                        textBuffer.moveCaretDownWithOption()
                    }
                }

                KeyEvent.VK_ENTER -> textBuffer.insertChar(newLineChar)
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
                textBuffer.insertChar(e.keyChar)
                repaint()
            }
        }

        private fun startBackspaceTimer() {
            backspaceTimer = Timer(backspaceRepeatRate) {
                if (backspacePressed) textBuffer.deleteBeforeCaret()
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
        repaint()
    }

    fun saveFile(file: File) {
        textBuffer.saveToFile(file)
    }
}