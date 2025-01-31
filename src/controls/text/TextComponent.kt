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
    val fontColor: Color = Color.BLACK,
    val selectionColor: Color = Color.PINK,
) : JComponent() {

    private val textBuffer = TextBuffer(newLineChar)
    private val caretModel = CaretModel(textBuffer)
    private val selectionModel = SelectionModel(textBuffer)
    private var caretVisible = true

    private var backspacePressed = false
    private var backspaceTimer: Timer? = null
    private var isMouseDragging = false

    init {
        font = Font(fontName, Font.PLAIN, fontSize)
        addKeyListener(TextKeyListener())
        addMouseListener(TextMouseListener())
        addMouseMotionListener(TextMouseMotionListener())
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

        selectionModel.getSelectionBounds()?.let { (start, end) ->
            g.color = selectionColor
            paintTextSelection(g, fm, start, end)
        }

        g.color = fontColor
        var y = lineHeight
        val lines = textBuffer.getLines()
        for (line in lines) {
            g.drawString(line, 5, y)
            y += lineHeight
        }

        if (caretVisible && !selectionModel.hasSelection) {
            val (caretX, caretY) = getCaretCoordinates(fm)
            g.drawLine(caretX + 5, caretY - fm.ascent, caretX + 5, caretY - fm.ascent + lineHeight)
        }
    }

    private fun paintTextSelection(g: Graphics, fm: FontMetrics, start: Int, end: Int) {
        val lineHeight = fm.ascent + fm.descent

        var currentPos = 0
        var y = lineHeight

        for (line in textBuffer.getLines()) {
            val lineStart = currentPos
            val lineEnd = lineStart + line.length

            if (end > lineStart && start < lineEnd + 1) {
                val selStart = maxOf(start - lineStart, 0)
                val selEnd = minOf(end - lineStart, line.length)

                val startX = fm.stringWidth(line.substring(0, selStart))
                val width = fm.stringWidth(line.substring(selStart, selEnd))

                g.fillRect(startX + 5, y - fm.ascent, width, lineHeight)
            }

            currentPos = lineEnd + 1
            y += lineHeight
        }
    }

    private fun getPositionFromPoint(point: Point): Int {
        val fm = getFontMetrics(font)
        val lineHeight = fm.ascent + fm.descent
        val clickY = point.y

        var currentY = lineHeight
        var currentPos = 0

        for (line in textBuffer.getLines()) {
            if (clickY < currentY) {
                val chars = line.toCharArray()
                var totalWidth = 0
                for (i in chars.indices) {
                    val charWidth = fm.charWidth(chars[i])
                    if (point.x - 5 <= totalWidth + charWidth / 2) {
                        return currentPos + i
                    }
                    totalWidth += charWidth
                }
                return currentPos + line.length
            }
            currentPos += line.length + 1
            currentY += lineHeight
        }

        return textBuffer.length
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
                    if (selectionModel.hasSelection) {
                        selectionModel.getSelectionBounds()?.let { (start, end) ->
                            for (i in end - 1 downTo start) {
                                textBuffer.deleteCharAt(i)
                            }
                            caretModel.moveTo(start)
                            selectionModel.clearSelection()
                        }
                    } else if (!backspacePressed) {
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
                    if (selectionModel.hasSelection) {
                        selectionModel.getSelectionBounds()?.let { (start, end) ->
                            for (i in end - 1 downTo start) {
                                textBuffer.deleteCharAt(i)
                            }
                            caretModel.moveTo(start)
                            selectionModel.clearSelection()
                        }
                    } else {
                        val position = caretModel.getCurrentPosition()
                        if (position.offset < textBuffer.length) {
                            textBuffer.deleteCharAt(position.offset)
                        }
                    }
                }

                KeyEvent.VK_LEFT -> {
                    selectionModel.clearSelection()
                    when {
                        e.isControlDown || e.isMetaDown -> caretModel.moveToLineStart()
                        e.isAltDown -> caretModel.moveToPreviousWord()
                        else -> caretModel.moveLeft()
                    }
                }

                KeyEvent.VK_RIGHT -> {
                    selectionModel.clearSelection()
                    when {
                        e.isControlDown || e.isMetaDown -> caretModel.moveToLineEnd()
                        e.isAltDown -> caretModel.moveToNextWord()
                        else -> caretModel.moveRight()
                    }
                }

                KeyEvent.VK_UP -> {
                    selectionModel.clearSelection()
                    when {
                        e.isControlDown || e.isMetaDown -> caretModel.moveToTextStart()
                        e.isAltDown -> caretModel.moveUpWithOption()
                    }
                }

                KeyEvent.VK_DOWN -> {
                    selectionModel.clearSelection()
                    when {
                        e.isControlDown || e.isMetaDown -> caretModel.moveToTextEnd()
                        e.isAltDown -> caretModel.moveDownWithOption()
                    }
                }

                KeyEvent.VK_ENTER -> {
                    if (selectionModel.hasSelection) {
                        selectionModel.getSelectionBounds()?.let { (start, end) ->
                            for (i in end - 1 downTo start) {
                                textBuffer.deleteCharAt(i)
                            }
                            caretModel.moveTo(start)
                            selectionModel.clearSelection()
                        }
                    }
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
                if (selectionModel.hasSelection) {
                    selectionModel.getSelectionBounds()?.let { (start, end) ->
                        for (i in end - 1 downTo start) {
                            textBuffer.deleteCharAt(i)
                        }
                        caretModel.moveTo(start)
                        selectionModel.clearSelection()
                    }
                }
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
        override fun mousePressed(e: MouseEvent) {
            requestFocusInWindow()
            val position = getPositionFromPoint(e.point)
            caretModel.moveTo(position)
            selectionModel.startSelection(position)
            isMouseDragging = true
            repaint()
        }

        override fun mouseReleased(e: MouseEvent) {
            isMouseDragging = false
            if (selectionModel.selectionStart == selectionModel.selectionEnd) {
                selectionModel.clearSelection()
            }
            repaint()
        }
    }

    private inner class TextMouseMotionListener : MouseMotionAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            if (isMouseDragging) {
                val position = getPositionFromPoint(e.point)
                selectionModel.updateSelection(position)
                caretModel.moveTo(position)
                repaint()
            }
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