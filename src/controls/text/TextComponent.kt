package controls.text

import java.awt.*
import java.awt.event.*
import javax.swing.JComponent
import javax.swing.Timer

class TextComponent(
    fontName: String = "Monospaced",
    fontSize: Int = 14,
    caretBlinkRate: Int = 500,
    private val backspaceRepeatRate: Int = 50,
    private val newLineChar: Char = '\n',
    private val fontColor: Color = Color.BLACK,
    private val selectionColor: Color = Color.PINK,
    private val padding: Int = 5,
) : JComponent() {

    private val textBuffer = TextBuffer(newLineChar)
    private val caretModel = CaretModel(textBuffer)
    private val selectionModel = SelectionModel(textBuffer)
    private var caretVisible = true
    private val caretBlinkTimer: Timer

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

        caretBlinkTimer = Timer(caretBlinkRate) {
            caretVisible = !caretVisible
            repaint()
        }.apply {
            isRepeats = true
        }
    }

    var text: String
        get() = textBuffer.getText()
        set(value) {
            textBuffer.clear()
            value.forEach { char ->
                textBuffer.insertChar(char, textBuffer.length)
            }
            caretModel.moveToTextEnd()
            selectionModel.clearSelection()
            repaint()
        }

    override fun addNotify() {
        super.addNotify()
        caretBlinkTimer.start()
    }

    override fun removeNotify() {
        super.removeNotify()
        caretBlinkTimer.stop()
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
            g.drawString(line, padding, y)
            y += lineHeight
        }

        if (caretVisible) {
            val (caretX, caretY) = getCaretCoordinates(fm)
            g.drawLine(caretX + padding, caretY - fm.ascent, caretX + padding, caretY - fm.ascent + lineHeight)
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

                if (line.isEmpty() && selStart == 0) {
                    g.fillRect(padding, y - fm.ascent, fm.charWidth(' '), lineHeight)
                } else {
                    val startX = fm.stringWidth(line.substring(0, selStart))
                    val width = fm.stringWidth(line.substring(selStart, selEnd))

                    g.fillRect(startX + padding, y - fm.ascent, width, lineHeight)
                }
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
                    if (point.x - padding <= totalWidth + charWidth / 2) {
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

    private fun restartCaretBlinking() {
        caretBlinkTimer.restart()
        caretVisible = true
        repaint()
    }

    private inner class TextKeyListener : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_BACK_SPACE -> handleBackspace()
                KeyEvent.VK_DELETE -> handleDelete()
                KeyEvent.VK_LEFT -> handleLeftKey(e)
                KeyEvent.VK_RIGHT -> handleRightKey(e)
                KeyEvent.VK_UP -> handleUpKey(e)
                KeyEvent.VK_DOWN -> handleDownKey(e)
                KeyEvent.VK_ENTER -> handleEnter()
                KeyEvent.VK_HOME -> caretModel.moveToTextStart()
                KeyEvent.VK_END -> caretModel.moveToTextEnd()
                //TODO: KeyEvent.VK_PAGE_UP. Implement scrolling and clipping first
                KeyEvent.VK_PAGE_UP -> throw NotImplementedError()
                //TODO: KeyEvent.VK_PAGE_DOWN. Implement scrolling and clipping first
                KeyEvent.VK_PAGE_DOWN -> throw NotImplementedError()
                KeyEvent.VK_A -> handleAKey(e)
                KeyEvent.VK_C -> handleCopy(e)
            }
            repaint()
        }

        private fun handleCopy(e: KeyEvent) {
            if ((e.isControlDown || e.isMetaDown) && selectionModel.hasSelection) {
                val selectedText = selectionModel.getSelectedText()
                val stringSelection = StringSelection(selectedText)
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(stringSelection, null)
            }
        }

        private fun handleAKey(e: KeyEvent) {
            if (e.isControlDown || e.isMetaDown) {
                selectionModel.startSelection(0)
                selectionModel.updateSelection(textBuffer.length)
                caretModel.moveTo(textBuffer.length)
                repaint()
            }
        }

        private fun handleBackspace() {
            if (selectionModel.hasSelection) {
                deleteSelectedText()
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

        private fun handleDelete() {
            if (selectionModel.hasSelection) {
                deleteSelectedText()
            } else {
                val position = caretModel.getCurrentPosition()
                if (position.offset < textBuffer.length) {
                    textBuffer.deleteCharAt(position.offset)
                }
            }
        }

        private fun handleLeftKey(e: KeyEvent) {
            if (!e.isShiftDown) {
                selectionModel.clearSelection()
            } else if (!selectionModel.hasSelection) {
                selectionModel.startSelection(caretModel.position)
            }

            when {
                e.isControlDown || e.isMetaDown -> caretModel.moveToLineStart()
                e.isAltDown -> caretModel.moveToPreviousWord()
                else -> caretModel.moveLeft()
            }

            if (e.isShiftDown) {
                selectionModel.updateSelection(caretModel.position)
            }

            restartCaretBlinking()
        }

        private fun handleRightKey(e: KeyEvent) {
            if (!e.isShiftDown) {
                selectionModel.clearSelection()
            } else if (!selectionModel.hasSelection) {
                selectionModel.startSelection(caretModel.position)
            }

            when {
                e.isControlDown || e.isMetaDown -> caretModel.moveToLineEnd()
                e.isAltDown -> caretModel.moveToNextWord()
                else -> caretModel.moveRight()
            }

            if (e.isShiftDown) {
                selectionModel.updateSelection(caretModel.position)
            }

            restartCaretBlinking()
        }

        private fun handleUpKey(e: KeyEvent) {
            if (!e.isShiftDown) {
                selectionModel.clearSelection()
            } else if (!selectionModel.hasSelection) {
                selectionModel.startSelection(caretModel.position)
            }

            val currentCaretPosition = caretModel.getCurrentPosition()
            val columnOffset = currentCaretPosition.offset - currentCaretPosition.lineStart

            if (currentCaretPosition.lineStart > 0) {
                val prevLineEnd = currentCaretPosition.lineStart - 1
                val prevLineStart = textBuffer.getText().lastIndexOf(newLineChar, prevLineEnd - 1) + 1
                val prevLineLength = prevLineEnd - prevLineStart

                val newPosition = when {
                    e.isControlDown || e.isMetaDown -> 0
                    else -> {
                        val newOffset = minOf(columnOffset, prevLineLength)
                        prevLineStart + newOffset
                    }
                }

                caretModel.moveTo(newPosition)
            } else if (e.isControlDown || e.isMetaDown) {
                caretModel.moveToTextStart()
            }

            if (e.isShiftDown) {
                selectionModel.updateSelection(caretModel.position)
            }

            restartCaretBlinking()
        }

        private fun handleDownKey(e: KeyEvent) {
            if (!e.isShiftDown) {
                selectionModel.clearSelection()
            } else if (!selectionModel.hasSelection) {
                selectionModel.startSelection(caretModel.position)
            }

            val currentCaretPosition = caretModel.getCurrentPosition()
            val columnOffset = currentCaretPosition.offset - currentCaretPosition.lineStart

            if (currentCaretPosition.lineEnd < textBuffer.length) {
                val nextLineStart = currentCaretPosition.lineEnd + 1
                val nextLineEnd = textBuffer.getText().indexOf(newLineChar, nextLineStart).let {
                    if (it == -1) textBuffer.length else it
                }
                val nextLineLength = nextLineEnd - nextLineStart

                val newPosition = when {
                    e.isControlDown || e.isMetaDown -> textBuffer.length
                    else -> {
                        val newOffset = minOf(columnOffset, nextLineLength)
                        nextLineStart + newOffset
                    }
                }

                caretModel.moveTo(newPosition)
            } else if (e.isControlDown || e.isMetaDown) {
                caretModel.moveToTextEnd()
            }

            if (e.isShiftDown) {
                selectionModel.updateSelection(caretModel.position)
            }
        }

        private fun handleEnter() {
            if (selectionModel.hasSelection) {
                deleteSelectedText()
            }
            val position = caretModel.getCurrentPosition()
            textBuffer.insertChar(newLineChar, position.offset)
            caretModel.moveRight()
        }

        private fun deleteSelectedText() {
            selectionModel.getSelectionBounds()?.let { (start, end) ->
                for (i in end - 1 downTo start) {
                    textBuffer.deleteCharAt(i)
                }
                caretModel.moveTo(start)
                selectionModel.clearSelection()
            }
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
                    deleteSelectedText()
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
            restartCaretBlinking()
        }
    }

    private inner class TextMouseListener : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            requestFocusInWindow()
            val position = getPositionFromPoint(e.point)

            if (e.isShiftDown && selectionModel.hasSelection) {
                selectionModel.updateSelection(position)
            } else {
                caretModel.moveTo(position)
                selectionModel.startSelection(position)
            }

            isMouseDragging = true
            restartCaretBlinking()
            repaint()
        }

        override fun mouseReleased(e: MouseEvent) {
            isMouseDragging = false
            if (selectionModel.selectionStart == selectionModel.selectionEnd && !e.isShiftDown) {
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
                restartCaretBlinking()
                repaint()
            }
        }
    }
}