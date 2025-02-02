package controls.text

import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import javax.swing.JComponent
import javax.swing.Timer
import kotlin.math.exp

private const val MULTI_CLICK_TIMEOUT_MS = 500

abstract class TextComponent(
    fontName: String,
    fontSize: Int,
    caretBlinkRate: Int,
    private val backspaceInitialRepeatRate: Int,
    private val backspaceAccelerationFactor: Double,
    private val backspaceRepeatMinRate: Int,
    private val newLineChar: Char,
    private val fontColor: Color,
    private val selectionColor: Color,
    private val padding: Int,
) : JComponent() {

    internal val textBuffer = TextBuffer(newLineChar)
    internal val caretModel = CaretModel(textBuffer)
    private val selectionModel = SelectionModel(textBuffer)
    private val undoManager = UndoManager()
    private var caretVisible = true
    private val caretBlinkTimer: Timer

    private var backspacePressed = false
    private var backspaceTimer: Timer? = null
    private var backspacePressStartTime: Long = 0
    private var isMouseDragging = false

    init {
        font = Font(fontName, Font.PLAIN, fontSize)
        this.addKeyListener(TextKeyListener())
        this.addMouseListener(TextMouseListener())
        this.addMouseMotionListener(TextMouseMotionListener())
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
            undoManager.clear()
            textBuffer.clear()
            value.forEach { char ->
                textBuffer.insertChar(char, textBuffer.length)
            }
            caretModel.moveToTextEnd()
            selectionModel.clearSelection()
            onTextChanged();
            ensureCaretVisible()
            repaint()
        }

    abstract fun onTextChanged()
    abstract fun ensureCaretVisible()

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

    protected open fun getPositionFromPoint(point: Point): Int {
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
                KeyEvent.VK_PAGE_UP -> handlePageUp(e)
                KeyEvent.VK_PAGE_DOWN -> handlePageDown(e)
                KeyEvent.VK_A -> handleAKey(e)
                KeyEvent.VK_C -> handleCopy(e)
                KeyEvent.VK_X -> handleCut(e)
                KeyEvent.VK_V -> handlePaste(e)
                KeyEvent.VK_Z -> handleUndo(e)
                KeyEvent.VK_Y -> handleRedo(e)
            }

            ensureCaretVisible()
            repaint()
        }

        private fun handlePageUp(e: KeyEvent) {
            if (!e.isShiftDown) {
                selectionModel.clearSelection()
            } else if (!selectionModel.hasSelection) {
                selectionModel.startSelection(caretModel.position)
            }

            val metrics = getFontMetrics(font)
            val linesPerPage = (height - 2 * padding) / metrics.height

            repeat(linesPerPage) {
                caretModel.moveUpWithOption()
            }

            if (e.isShiftDown) {
                selectionModel.updateSelection(caretModel.position)
            }
        }

        private fun handlePageDown(e: KeyEvent) {
            if (!e.isShiftDown) {
                selectionModel.clearSelection()
            } else if (!selectionModel.hasSelection) {
                selectionModel.startSelection(caretModel.position)
            }

            val metrics = getFontMetrics(font)
            val linesPerPage = (height - 2 * padding) / metrics.height

            repeat(linesPerPage) {
                caretModel.moveDownWithOption()
            }

            if (e.isShiftDown) {
                selectionModel.updateSelection(caretModel.position)
            }
        }

        private fun handleUndo(e: KeyEvent) {
            if (e.isControlDown || e.isMetaDown) {
                if (undoManager.undo(textBuffer, caretModel)) {
                    selectionModel.clearSelection()
                    repaint()
                }
            }
        }

        private fun handleRedo(e: KeyEvent) {
            if (e.isControlDown || e.isMetaDown) {
                if (undoManager.redo(textBuffer, caretModel)) {
                    selectionModel.clearSelection()
                    repaint()
                }
            }
        }

        private fun handlePaste(e: KeyEvent) {
            if (e.isControlDown || e.isMetaDown) {
                try {
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    val data = clipboard.getData(DataFlavor.stringFlavor) as? String
                    if (data != null) {
                        if (selectionModel.hasSelection) {
                            val selectedText = selectionModel.getSelectedText()
                            val selectionStart = minOf(selectionModel.selectionStart, selectionModel.selectionEnd)
                            val position = caretModel.getCurrentPosition()
                            undoManager.addEdit(TextAction.Delete(selectionStart, selectedText, position.offset))
                            deleteSelectedText()
                        }

                        val position = caretModel.getCurrentPosition()
                        undoManager.addEdit(TextAction.Insert(position.offset, data, position.offset))

                        var insertOffset = position.offset
                        data.forEach { char ->
                            textBuffer.insertChar(char, insertOffset)
                            insertOffset++
                        }

                        caretModel.moveTo(insertOffset)
                        repaint()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun handleCut(e: KeyEvent) {
            if ((e.isControlDown || e.isMetaDown) && selectionModel.hasSelection) {
                val selectedText = selectionModel.getSelectedText()

                val stringSelection = StringSelection(selectedText)
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(stringSelection, null)

                val selectionStart = minOf(selectionModel.selectionStart, selectionModel.selectionEnd)
                val position = caretModel.getCurrentPosition()
                undoManager.addEdit(TextAction.Delete(selectionStart, selectedText, position.offset))

                deleteSelectedText()
                repaint()
            }
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
                val selectedText = selectionModel.getSelectedText()
                val selectionStart = minOf(selectionModel.selectionStart, selectionModel.selectionEnd)
                val position = caretModel.getCurrentPosition()
                undoManager.addEdit(TextAction.Delete(selectionStart, selectedText, position.offset))
                deleteSelectedText()
            } else if (!backspacePressed) {
                backspacePressed = true
                backspacePressStartTime = System.currentTimeMillis()
                val position = caretModel.getCurrentPosition()
                if (position.offset > 0) {
                    val deletedChar = textBuffer.charAt(position.offset - 1)
                    undoManager.addEdit(TextAction.Delete(position.offset - 1, deletedChar.toString(), position.offset))
                    textBuffer.deleteCharAt(position.offset - 1)
                    caretModel.moveLeft()
                }
                startBackspaceTimer()
            }
        }

        private fun handleDelete() {
            val position = caretModel.getCurrentPosition()
            if (selectionModel.hasSelection) {
                val selectedText = selectionModel.getSelectedText()
                val selectionStart = minOf(selectionModel.selectionStart, selectionModel.selectionEnd)
                undoManager.addEdit(TextAction.Delete(selectionStart, selectedText, position.offset))
                deleteSelectedText()
            } else {
                if (position.offset < textBuffer.length) {
                    val deletedChar = textBuffer.charAt(position.offset)
                    undoManager.addEdit(TextAction.Delete(position.offset, deletedChar.toString(), position.offset))
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
            val columnOffset = currentCaretPosition.offset - currentCaretPosition.start

            if (currentCaretPosition.start > 0) {
                val prevLineEnd = currentCaretPosition.start - 1
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
            val columnOffset = currentCaretPosition.offset - currentCaretPosition.start

            if (currentCaretPosition.end < textBuffer.length) {
                val nextLineStart = currentCaretPosition.end + 1
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
                backspaceTimer = null
            }
        }

        override fun keyTyped(e: KeyEvent) {
            if (e.keyChar != KeyEvent.CHAR_UNDEFINED && e.keyChar != '\b' && !(e.isControlDown || e.isMetaDown) && e.keyChar != newLineChar) {
                if (selectionModel.hasSelection) {
                    val selectedText = selectionModel.getSelectedText()
                    val selectionStart = minOf(selectionModel.selectionStart, selectionModel.selectionEnd)
                    val position = caretModel.getCurrentPosition()
                    undoManager.addEdit(TextAction.Delete(selectionStart, selectedText, position.offset))
                    deleteSelectedText()
                }

                val position = caretModel.getCurrentPosition()
                textBuffer.insertChar(e.keyChar, position.offset)
                undoManager.addEdit(TextAction.Insert(position.offset, e.keyChar.toString(), position.offset))
                caretModel.moveRight()
                ensureCaretVisible()
                repaint()
            }
        }

        private fun calculateCurrentDelay(): Int {
            val elapsedTime = System.currentTimeMillis() - backspacePressStartTime
            val factor = exp(-backspaceAccelerationFactor * elapsedTime / 1000.0)
            val currentRate = (backspaceInitialRepeatRate * factor + backspaceRepeatMinRate).toInt()
            return currentRate.coerceIn(backspaceRepeatMinRate, backspaceInitialRepeatRate)
        }

        private fun startBackspaceTimer() {
            backspaceTimer?.stop()
            backspaceTimer = Timer(backspaceInitialRepeatRate) {
                if (backspacePressed) {
                    val position = caretModel.getCurrentPosition()
                    if (position.offset > 0) {
                        textBuffer.deleteCharAt(position.offset - 1)
                        caretModel.moveLeft()
                        repaint()

                        val currentDelay = calculateCurrentDelay()
                        backspaceTimer?.delay = currentDelay
                    }
                } else {
                    backspaceTimer?.stop()
                    backspaceTimer = null
                }
            }.apply { start() }
            restartCaretBlinking()
        }
    }

    private inner class TextMouseListener : MouseAdapter() {
        private var lastClickTime = 0L
        private var clickCount = 0

        override fun mousePressed(e: MouseEvent) {
            requestFocusInWindow()
            val position = getPositionFromPoint(e.point)

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > MULTI_CLICK_TIMEOUT_MS) {
                clickCount = 1
            } else {
                clickCount++
            }

            lastClickTime = currentTime

            when (clickCount) {
                1 -> {
                    if (e.isShiftDown && selectionModel.hasSelection) {
                        selectionModel.updateSelection(position)
                    } else {
                        caretModel.moveTo(position)
                        selectionModel.startSelection(position)
                    }
                }

                2 -> {
                    val (_, wordEnd) = selectionModel.selectWord(position)
                    caretModel.moveTo(wordEnd)
                }

                3 -> {
                    val caretPosition = caretModel.getCurrentPosition()
                    selectionModel.selectLine(caretPosition.start, caretPosition.end)
                    caretModel.moveTo(caretPosition.end)
                    clickCount = 0
                }
            }

            isMouseDragging = true
            restartCaretBlinking()
            ensureCaretVisible()
            repaint()
        }

        override fun mouseReleased(e: MouseEvent) {
            isMouseDragging = false
            if (clickCount == 1 && selectionModel.selectionStart == selectionModel.selectionEnd && !e.isShiftDown) {
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
                ensureCaretVisible()
                repaint()
            }
        }
    }
}