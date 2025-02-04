package controls.text

import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Point
import java.awt.event.*
import javax.swing.JComponent
import javax.swing.Timer

abstract class TextComponent(
    fontName: String,
    fontSize: Int,
    fontColor: Color,
    selectionColor: Color,
    private val padding: Int,
    caretWidth: Int = Caret.WIDTH,
    caretColor: Color = Caret.COLOR,
) : JComponent() {
    protected companion object {
        private const val MULTI_CLICK_TIMEOUT_MS = 500
        private const val NEW_LINE_CHAR = '\n'

        object Font {
            const val NAME = "Monospaced"
            const val SIZE = 14
            val COLOR: Color = Color.BLACK
            val SELECTION_COLOR: Color = Color.PINK
        }

        object Caret {
            const val BLINK_RATE = 500
            const val REPEAT_INITIAL_DELAY = 250
            const val REPEAT_ACCELERATION_FACTOR = 0.8
            const val REPEAT_MIN_DELAY = 1
            const val WIDTH = 2
            val COLOR: Color = Color.BLACK
        }

        object Layout {
            const val PADDING = 5
        }
    }

    private val undoManager = UndoManager()
    private val caretBlinkTimer: Timer
    private var isMouseDragging = false
    protected var caretVisible = true
    internal val textBuffer = TextBuffer(NEW_LINE_CHAR)
    internal val caretModel = CaretModel(textBuffer)
    internal val selectionModel = SelectionModel(textBuffer)
    internal val textRenderer = TextRenderer(
        textBuffer = textBuffer,
        caretModel = caretModel,
        selectionModel = selectionModel,
        padding = padding,
        fontColor = fontColor,
        selectionColor = selectionColor,
        caretWidth = caretWidth,
        caretColor = caretColor
    )

    private val backspaceAction = RepeatableAction(
        initialDelay = Caret.REPEAT_INITIAL_DELAY,
        accelerationFactor = Caret.REPEAT_ACCELERATION_FACTOR,
        minDelay = Caret.REPEAT_MIN_DELAY
    ) {
        val position = caretModel.getCurrentPosition()
        textBuffer.deleteChar(position.offset, true, undoManager, caretModel)
        repaint()
    }

    private val deleteAction = RepeatableAction(
        initialDelay = Caret.REPEAT_INITIAL_DELAY,
        accelerationFactor = Caret.REPEAT_ACCELERATION_FACTOR,
        minDelay = Caret.REPEAT_MIN_DELAY
    ) {
        val position = caretModel.getCurrentPosition()
        textBuffer.deleteChar(position.offset, false, undoManager, caretModel)
        repaint()
    }

    private val undoAction = RepeatableAction(
        initialDelay = Caret.REPEAT_INITIAL_DELAY,
        accelerationFactor = Caret.REPEAT_ACCELERATION_FACTOR,
        minDelay = Caret.REPEAT_MIN_DELAY
    ) {
        if (undoManager.undo(textBuffer, caretModel)) {
            selectionModel.clearSelection()
            repaint()
        }
    }

    private val redoAction = RepeatableAction(
        initialDelay = Caret.REPEAT_INITIAL_DELAY,
        accelerationFactor = Caret.REPEAT_ACCELERATION_FACTOR,
        minDelay = Caret.REPEAT_MIN_DELAY
    ) {
        if (undoManager.redo(textBuffer, caretModel)) {
            selectionModel.clearSelection()
            repaint()
        }
    }

    init {
        font = Font(fontName, java.awt.Font.PLAIN, fontSize)
        this.addKeyListener(TextKeyListener())
        this.addMouseListener(TextMouseListener())
        this.addMouseMotionListener(TextMouseMotionListener())
        isFocusable = true
        focusTraversalKeysEnabled = false

        caretBlinkTimer = Timer(Caret.BLINK_RATE) {
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
            onTextChanged()
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

        val context = TextRenderer.RenderContext(
            graphics = g, clip = g.clipBounds, width = width, height = height, caretVisible = caretVisible
        )

        textRenderer.render(context)
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

    private fun restartCaretBlinking() {
        caretBlinkTimer.restart()
        caretVisible = true
        repaint()
    }

    private inner class TextKeyListener : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_LEFT -> {
                    e.handleShiftSelectionOnNavigation(selectionModel, caretModel) {
                        when {
                            e.isControlDown || e.isMetaDown -> caretModel.moveToLineStart()
                            e.isAltDown -> caretModel.moveToPreviousWord()
                            else -> caretModel.moveLeft()
                        }
                    }
                }

                KeyEvent.VK_RIGHT -> {
                    e.handleShiftSelectionOnNavigation(selectionModel, caretModel) {
                        when {
                            e.isControlDown || e.isMetaDown -> caretModel.moveToLineEnd()
                            e.isAltDown -> caretModel.moveToNextWord()
                            else -> caretModel.moveRight()
                        }
                    }

                }

                KeyEvent.VK_UP -> {
                    e.handleShiftSelectionOnNavigation(selectionModel, caretModel) {
                        when {
                            e.isControlDown || e.isMetaDown -> caretModel.moveToTextStart()
                            e.isAltDown -> caretModel.moveToPreviousLineStart()
                            else -> caretModel.moveCursorUpPreserveColumn()
                        }
                    }
                }

                KeyEvent.VK_DOWN -> {
                    e.handleShiftSelectionOnNavigation(selectionModel, caretModel) {
                        when {
                            e.isControlDown || e.isMetaDown -> caretModel.moveToTextEnd()
                            e.isAltDown -> caretModel.moveToNextLineEnd()
                            else -> caretModel.moveCursorDownPreserveColumn()
                        }
                    }
                }

                KeyEvent.VK_ENTER -> handleEnter()
                KeyEvent.VK_HOME -> {
                    e.handleShiftSelectionOnNavigation(selectionModel = selectionModel, caretModel = caretModel) {
                        caretModel.moveToTextStart()
                    }
                }

                KeyEvent.VK_END -> {
                    e.handleShiftSelectionOnNavigation(selectionModel = selectionModel, caretModel = caretModel) {
                        caretModel.moveToTextEnd()
                    }
                }

                KeyEvent.VK_PAGE_UP -> {
                    e.handleShiftSelectionOnNavigation(selectionModel = selectionModel, caretModel = caretModel) {
                        val linesPerPage = getFontMetrics(font).calculateLinesPerPage(height, padding)
                        repeat(linesPerPage) { caretModel.moveCursorUpPreserveColumn() }
                    }
                }

                KeyEvent.VK_PAGE_DOWN -> {
                    e.handleShiftSelectionOnNavigation(selectionModel = selectionModel, caretModel = caretModel) {
                        val linesPerPage = getFontMetrics(font).calculateLinesPerPage(height, padding)
                        repeat(linesPerPage) { caretModel.moveCursorDownPreserveColumn() }
                    }
                }

                KeyEvent.VK_A -> {
                    if (e.isControlDown || e.isMetaDown) {
                        handleSelectAll()
                    }
                }

                KeyEvent.VK_C -> e.handleClipboardCopy(selectionModel)
                KeyEvent.VK_X -> e.handleClipboardCut(selectionModel, textBuffer, caretModel, undoManager)
                KeyEvent.VK_V -> e.handleClipboardPaste(selectionModel, textBuffer, caretModel, undoManager)
                KeyEvent.VK_BACK_SPACE -> {
                    if (selectionModel.hasSelection) {
                        selectionModel.deleteSelectedText(textBuffer, caretModel, undoManager)
                    } else {
                        backspaceAction.start()
                    }
                }

                KeyEvent.VK_DELETE -> {
                    if (selectionModel.hasSelection) {
                        selectionModel.deleteSelectedText(textBuffer, caretModel, undoManager)
                    } else {
                        deleteAction.start()
                    }
                }

                KeyEvent.VK_Z -> {
                    if (e.isControlDown || e.isMetaDown) {
                        undoAction.start()
                    }
                }

                KeyEvent.VK_Y -> {
                    if (e.isControlDown || e.isMetaDown) {
                        redoAction.start()
                    }
                }

                else -> return
            }

            ensureCaretVisible()
            restartCaretBlinking()
            repaint()
        }

        fun handleSelectAll() {
            with(selectionModel) {
                startSelection(0)
                updateSelection(textBuffer.length)
            }
            caretModel.moveTo(textBuffer.length)
        }

        fun handleEnter() {
            selectionModel.takeIf { it.hasSelection }?.deleteSelectedText(textBuffer, caretModel, undoManager)

            val position = caretModel.getCurrentPosition()
            undoManager.addEdit(TextAction.Insert(position.offset, NEW_LINE_CHAR.toString(), position.offset))
            textBuffer.insertChar(NEW_LINE_CHAR, position.offset)
            caretModel.moveRight()
        }

        override fun keyReleased(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_BACK_SPACE -> backspaceAction.stop()
                KeyEvent.VK_DELETE -> deleteAction.stop()
                KeyEvent.VK_Z -> undoAction.stop()
                KeyEvent.VK_Y -> redoAction.stop()
            }
        }

        override fun keyTyped(e: KeyEvent) {
            if (e.keyChar != KeyEvent.CHAR_UNDEFINED && e.keyChar != '\b' && !(e.isControlDown || e.isMetaDown) && e.keyChar != NEW_LINE_CHAR) {
                if (selectionModel.hasSelection) {
                    selectionModel.deleteSelectedText(textBuffer, caretModel, undoManager)
                }

                val position = caretModel.getCurrentPosition()
                textBuffer.insertChar(e.keyChar, position.offset)
                undoManager.addEdit(TextAction.Insert(position.offset, e.keyChar.toString(), position.offset))
                caretModel.moveRight()
                ensureCaretVisible()
                restartCaretBlinking()
                repaint()
            }
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
                    if (e.isShiftDown) {
                        if (!selectionModel.hasSelection) {
                            val currentPosition = caretModel.getCurrentPosition().offset
                            selectionModel.startSelection(currentPosition)
                        }
                        selectionModel.updateSelection(position)
                        caretModel.moveTo(position)
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