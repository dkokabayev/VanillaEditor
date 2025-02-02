package controls.text

import java.awt.Color
import java.awt.Graphics
import java.awt.Point
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import kotlin.math.max
import kotlin.math.roundToInt

class TextArea(
    fontName: String = "Monospaced",
    fontSize: Int = 14,
    caretBlinkRate: Int = 500,
    backspaceInitialRepeatRate: Int = 250,
    backspaceAccelerationFactor: Double = 0.8,
    backspaceRepeatMinRate: Int = 1,
    fontColor: Color = Color.BLACK,
    selectionColor: Color = Color.PINK,
    private val newLineChar: Char = '\n',
    private val padding: Int = 5,
    private val scrollBarWidth: Int = 15,
    private val scrollBarColor: Color = Color.lightGray,
    private val scrollBarHoverColor: Color = Color.gray,
    private val scrollBarDragColor: Color = Color(110, 110, 110),
    private val scrollBarBackgroundColor: Color = Color(230, 230, 230)
) : TextComponent(
    fontName,
    fontSize,
    caretBlinkRate,
    backspaceInitialRepeatRate,
    backspaceAccelerationFactor,
    backspaceRepeatMinRate,
    newLineChar,
    fontColor,
    selectionColor,
    padding
) {
    private data class LineInfo(val startOffset: Int, val length: Int)
    private data class TextGridPosition(val line: Int, val column: Int)
    private data class ScrollBarMetrics(val height: Int, val y: Int, val isVisible: Boolean)

    private val scrollModel = ScrollModel()
    private val lineCache = mutableListOf<LineInfo>()
    private var lastKnownTextLength = 0

    init {
        updateLineCache()

        addMouseWheelListener { e ->
            val scrollAmount = e.preciseWheelRotation * getFontMetrics(font).height
            scrollModel.scrollY = (scrollModel.scrollY + scrollAmount.roundToInt()).coerceIn(0, scrollModel.maxScrollY)
            repaint()
        }

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                updateScrollBarHover(e.point)
            }

            override fun mouseDragged(e: MouseEvent) {
                scrollModel.updateDragPosition(e.y, height, getContentHeight())
                repaint()
            }
        })
    }

    private fun calculateScrollBarMetrics(): ScrollBarMetrics {
        val contentHeight = getContentHeight()
        val calculatedScrollBarHeight = (height.toFloat() * height / contentHeight).toInt()
        val scrollBarHeight = maxOf(calculatedScrollBarHeight, scrollBarWidth)
        val maxScrollBarY = height - scrollBarHeight
        val scrollBarY =
            if (maxScrollBarY <= 0) 0 else (scrollModel.scrollY.toFloat() * maxScrollBarY / scrollModel.maxScrollY).toInt()

        return ScrollBarMetrics(
            height = scrollBarHeight, y = scrollBarY, isVisible = contentHeight > height
        )
    }

    override fun processMouseEvent(e: MouseEvent) {
        when (e.id) {
            MouseEvent.MOUSE_PRESSED -> {
                if (handleScrollBarClick(e.point)) return
            }

            MouseEvent.MOUSE_RELEASED -> {
                if (handleScrollBarRelease()) return
            }

            MouseEvent.MOUSE_ENTERED -> updateScrollBarHover(e.point)
            MouseEvent.MOUSE_EXITED -> {
                scrollModel.isHovered = false
                repaint()
            }
        }
        super.processMouseEvent(e)
    }

    private fun handleScrollBarClick(point: Point): Boolean {
        if (!isInScrollBarArea(point)) return false

        val metrics = calculateScrollBarMetrics()
        if (isInScrollBarThumb(point, metrics)) {
            scrollModel.startDragging(point.y)
        } else {
            val clickPercent = (point.y - metrics.height / 2).toFloat() / (height - metrics.height)
            scrollModel.scrollY = (clickPercent * scrollModel.maxScrollY).toInt().coerceIn(0, scrollModel.maxScrollY)
        }
        repaint()
        return true
    }

    private fun handleScrollBarRelease(): Boolean {
        if (!scrollModel.isDragging) return false
        scrollModel.stopDragging()
        repaint()
        return true
    }

    private fun updateLineCache() {
        val text = textBuffer.getText()
        if (text.length == lastKnownTextLength) return

        lineCache.clear()
        var currentLineStart = 0
        var currentPos = 0

        while (currentPos < text.length) {
            if (text[currentPos] == newLineChar) {
                lineCache.add(LineInfo(currentLineStart, currentPos - currentLineStart))
                currentLineStart = currentPos + 1
            }
            currentPos++
        }

        if (currentLineStart <= text.length) {
            lineCache.add(LineInfo(currentLineStart, text.length - currentLineStart))
        }

        lastKnownTextLength = text.length
    }

    private fun updateScrollBarHover(point: Point) {
        if (scrollModel.updateHover(point, width, scrollBarWidth, getContentHeight(), height)) {
            repaint()
        }
    }

    private fun isInScrollBarArea(point: Point): Boolean =
        point.x >= width - scrollBarWidth && getContentHeight() > height

    private fun isInScrollBarThumb(point: Point, metrics: ScrollBarMetrics): Boolean =
        isInScrollBarArea(point) && point.y >= metrics.y && point.y <= metrics.y + metrics.height

    private fun getContentHeight(): Int {
        updateLineCache()
        val lineHeight = getFontMetrics(font).height
        return lineHeight * textBuffer.getLines().size + padding * 2
    }

    override fun onTextChanged() {
        updateLineCache()
    }

    override fun paintComponent(g: Graphics) {
        updateLineCache()
        paintContent(g)
        paintScrollBar(g)
    }

    private fun paintContent(g: Graphics) {
        g.clipRect(0, 0, width - scrollBarWidth, height)
        g.translate(0, -scrollModel.scrollY)

        super.paintComponent(g)

        g.translate(0, scrollModel.scrollY)
        g.clip = null
    }

    private fun paintScrollBar(g: Graphics) {
        val metrics = calculateScrollBarMetrics()
        if (!metrics.isVisible) return

        scrollModel.maxScrollY = max(0, getContentHeight() - height)

        g.color = scrollBarBackgroundColor
        g.fillRect(width - scrollBarWidth, 0, scrollBarWidth, height)

        g.color = when {
            scrollModel.isDragging -> scrollBarDragColor
            scrollModel.isHovered -> scrollBarHoverColor
            else -> scrollBarColor
        }
        g.fillRect(width - scrollBarWidth + 2, metrics.y, scrollBarWidth - 4, metrics.height)
    }

    override fun getPositionFromPoint(point: Point): Int {
        val adjustedPoint = Point(point.x, point.y + scrollModel.scrollY)
        return super.getPositionFromPoint(adjustedPoint)
    }

    override fun ensureCaretVisible() {
        val lineHeight = getFontMetrics(font).height
        val textGridPosition = getCurrentTextGridPosition()
        val caretY = lineHeight * textGridPosition.line

        scrollModel.scrollY = when {
            caretY < scrollModel.scrollY -> caretY
            caretY + lineHeight > scrollModel.scrollY + height ->
                caretY + lineHeight + padding * 2 - height

            else -> scrollModel.scrollY
        }.coerceIn(0, scrollModel.maxScrollY)
    }

    private fun getCurrentTextGridPosition(): TextGridPosition {
        updateLineCache()
        val caretOffset = caretModel.getCurrentPosition().offset

        for ((index, lineInfo) in lineCache.withIndex()) {
            val lineEnd = lineInfo.startOffset + lineInfo.length
            if (caretOffset <= lineEnd) {
                return TextGridPosition(index, caretOffset - lineInfo.startOffset)
            }
        }

        return TextGridPosition(lineCache.size - 1, lineCache.lastOrNull()?.length ?: 0)
    }
}