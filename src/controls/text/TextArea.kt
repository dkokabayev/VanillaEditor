package controls.text

import java.awt.Color
import java.awt.Graphics
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.SwingUtilities
import kotlin.math.roundToInt

class TextArea(
    fontName: String = TextComponent.Companion.Font.NAME,
    fontSize: Int = TextComponent.Companion.Font.SIZE,
    foregroundColor: Color = TextComponent.Companion.Font.COLOR,
    selectionColor: Color = TextComponent.Companion.Font.SELECTION_COLOR,
    scrollBarWidth: Int = ScrollBar.WIDTH,
    scrollBarColor: Color = ScrollBar.COLOR,
    scrollBarHoverColor: Color = ScrollBar.HOVER_COLOR,
    scrollBarDragColor: Color = ScrollBar.DRAG_COLOR,
    scrollBarBackgroundColor: Color = ScrollBar.BACKGROUND_COLOR,
    private val padding: Int = TextComponent.Companion.Layout.PADDING,
    caretWidth: Int = TextComponent.Companion.Caret.WIDTH,
    caretColor: Color = TextComponent.Companion.Caret.COLOR,
    lineNumbersVisible: Boolean = true,
    lineNumbersColumnColor: Color = LineNumbersColumn.COLOR,
    lineNumbersColumnBackgroundColor: Color = LineNumbersColumn.BACKGROUND_COLOR
) : TextComponent(
    fontName, fontSize, foregroundColor, selectionColor, padding, caretWidth, caretColor
) {
    private companion object {
        object ScrollBar {
            const val WIDTH = 12
            const val CORNER_RADIUS = 4
            val COLOR: Color = Color.lightGray
            val HOVER_COLOR: Color = Color.gray
            val DRAG_COLOR = Color(110, 110, 110)
            val BACKGROUND_COLOR = Color(230, 230, 230)
        }

        object LineNumbersColumn {
            val COLOR: Color = Color.GRAY
            val BACKGROUND_COLOR = Color(230, 230, 230)
        }
    }

    var foregroundColor: Color = foregroundColor
        set(value) {
            field = value
            textRenderer.foregroundColor = value
            repaint()
        }

    var selectionColor: Color = selectionColor
        set(value) {
            field = value
            textRenderer.selectionColor = value
            repaint()
        }

    var caretColor: Color = caretColor
        set(value) {
            field = value
            textRenderer.caretColor = value
            repaint()
        }

    var scrollBarColor: Color = scrollBarColor
        set(value) {
            field = value
            verticalScrollBar.color = value
            horizontalScrollBar.color = value
            repaint()
        }

    var scrollBarHoverColor: Color = scrollBarHoverColor
        set(value) {
            field = value
            verticalScrollBar.hoverColor = value
            horizontalScrollBar.hoverColor = value
            repaint()
        }

    var scrollBarDragColor: Color = scrollBarDragColor
        set(value) {
            field = value
            verticalScrollBar.dragColor = value
            horizontalScrollBar.dragColor = value
            repaint()
        }

    var scrollBarBackgroundColor: Color = scrollBarBackgroundColor
        set(value) {
            field = value
            verticalScrollBar.backgroundColor = value
            horizontalScrollBar.backgroundColor = value
            repaint()
        }

    var lineNumbersColumnColor: Color = lineNumbersColumnColor
        set(value) {
            field = value
            lineNumbersRenderer.color = value
            repaint()
        }

    var lineNumbersColumnBackgroundColor: Color = lineNumbersColumnBackgroundColor
        set(value) {
            field = value
            lineNumbersRenderer.backgroundColor = value
            repaint()
        }

    private val lineNumbersRenderer = LineNumbersRenderer(
        lineNumbersVisible, padding, font, lineNumbersColumnColor, lineNumbersColumnBackgroundColor
    )

    private val scrollModel = ScrollModel()
    private val verticalScrollBar = ScrollBarModel(
        width = scrollBarWidth,
        color = scrollBarColor,
        hoverColor = scrollBarHoverColor,
        dragColor = scrollBarDragColor,
        backgroundColor = scrollBarBackgroundColor,
        cornerRadius = ScrollBar.CORNER_RADIUS
    )
    private val horizontalScrollBar = ScrollBarModel(
        width = scrollBarWidth,
        color = scrollBarColor,
        hoverColor = scrollBarHoverColor,
        dragColor = scrollBarDragColor,
        backgroundColor = scrollBarBackgroundColor,
        cornerRadius = ScrollBar.CORNER_RADIUS
    )

    var lineNumbersVisible = lineNumbersVisible
        set(value) {
            field = value
            lineNumbersRenderer.isVisible = value
            repaint()
        }

    init {
        addMouseWheelListener { e ->
            if (e.isShiftDown) {
                val scrollAmount = e.preciseWheelRotation * getFontMetrics(font).charWidth('m')
                scrollModel.scrollX =
                    (scrollModel.scrollX + scrollAmount.roundToInt()).coerceIn(0, scrollModel.maxScrollX)
            } else {
                val scrollAmount = e.preciseWheelRotation * getFontMetrics(font).height
                scrollModel.scrollY =
                    (scrollModel.scrollY + scrollAmount.roundToInt()).coerceIn(0, scrollModel.maxScrollY)
            }
            repaint()
        }

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                updateScrollBarsHover(e.point)
            }

            override fun mouseDragged(e: MouseEvent) {
                if (scrollModel.isVerticalDragging) {
                    scrollModel.updateVerticalDragPosition(
                        e.y, height - horizontalScrollBar.getWidth(), getContentHeight()
                    )
                    repaint()
                }
                if (scrollModel.isHorizontalDragging) {
                    scrollModel.updateHorizontalDragPosition(
                        e.x, width - verticalScrollBar.getWidth(), getContentWidth()
                    )
                    repaint()
                }
            }
        })
    }

    private data class ScrollBarArea(val x: Int, val y: Int, val width: Int, val height: Int) {
        fun contains(point: Point) = point.x in x..(x + width) && point.y in y..(y + height)
    }

    private fun getVerticalScrollBarArea() = ScrollBarArea(
        x = width - verticalScrollBar.getWidth(),
        y = 0,
        width = verticalScrollBar.getWidth(),
        height = height - horizontalScrollBar.getWidth()
    )

    private fun getHorizontalScrollBarArea() = ScrollBarArea(
        x = 0,
        y = height - horizontalScrollBar.getWidth(),
        width = width - verticalScrollBar.getWidth(),
        height = horizontalScrollBar.getWidth()
    )

    private fun updateScrollBarsHover(point: Point) {
        val wasVerticalHovered = scrollModel.isVerticalHovered
        val wasHorizontalHovered = scrollModel.isHorizontalHovered

        scrollModel.isVerticalHovered = getVerticalScrollBarArea().contains(point)
        scrollModel.isHorizontalHovered = getHorizontalScrollBarArea().contains(point)

        if (wasVerticalHovered != scrollModel.isVerticalHovered || wasHorizontalHovered != scrollModel.isHorizontalHovered) {
            repaint()
        }
    }

    override fun processMouseEvent(e: MouseEvent) {
        when (e.id) {
            MouseEvent.MOUSE_PRESSED -> {
                if (handleVerticalScrollBarClick(e.point) || handleHorizontalScrollBarClick(e.point)) return
            }

            MouseEvent.MOUSE_RELEASED -> {
                if (scrollModel.isVerticalDragging || scrollModel.isHorizontalDragging) {
                    scrollModel.stopDragging()
                    repaint()
                    return
                }
            }

            MouseEvent.MOUSE_ENTERED -> updateScrollBarsHover(e.point)
            MouseEvent.MOUSE_EXITED -> {
                scrollModel.isVerticalHovered = false
                scrollModel.isHorizontalHovered = false
                repaint()
            }
        }
        super.processMouseEvent(e)
    }

    private fun handleVerticalScrollBarClick(point: Point): Boolean {
        val verticalBarArea = Rectangle(
            width - verticalScrollBar.getWidth(),
            0,
            verticalScrollBar.getWidth(),
            height - horizontalScrollBar.getWidth()
        )

        if (!verticalBarArea.contains(point)) return false

        val metrics = verticalScrollBar.calculateMetrics(
            viewportSize = height - horizontalScrollBar.getWidth(),
            contentSize = getContentHeight(),
            scroll = scrollModel.scrollY,
            maxScroll = scrollModel.maxScrollY
        )

        val thumbRect = Rectangle(
            width - verticalScrollBar.getWidth(), metrics.thumbPosition, verticalScrollBar.getWidth(), metrics.thumbSize
        )

        if (thumbRect.contains(point)) {
            scrollModel.startVerticalDragging(point.y)
        } else {
            scrollModel.scrollY = verticalScrollBar.calculateScrollPositionFromClick(
                click = point.y,
                metrics = metrics,
                viewportSize = height - horizontalScrollBar.getWidth(),
                maxScroll = scrollModel.maxScrollY
            )
        }
        repaint()
        return true
    }

    private fun handleHorizontalScrollBarClick(point: Point): Boolean {
        val horizontalBarArea = Rectangle(
            0,
            height - horizontalScrollBar.getWidth(),
            width - verticalScrollBar.getWidth(),
            horizontalScrollBar.getWidth()
        )

        if (!horizontalBarArea.contains(point)) return false

        val metrics = horizontalScrollBar.calculateMetrics(
            viewportSize = width - verticalScrollBar.getWidth(),
            contentSize = getContentWidth(),
            scroll = scrollModel.scrollX,
            maxScroll = scrollModel.maxScrollX
        )

        val thumbRect = Rectangle(
            metrics.thumbPosition,
            height - horizontalScrollBar.getWidth(),
            metrics.thumbSize,
            horizontalScrollBar.getWidth()
        )

        if (thumbRect.contains(point)) {
            scrollModel.startHorizontalDragging(point.x)
        } else {
            scrollModel.scrollX = horizontalScrollBar.calculateScrollPositionFromClick(
                click = point.x,
                metrics = metrics,
                viewportSize = width - verticalScrollBar.getWidth(),
                maxScroll = scrollModel.maxScrollX
            )
        }
        repaint()
        return true
    }

    override fun getPositionFromPoint(point: Point): Int {
        val fm = getFontMetrics(font)
        val lineCount = textBuffer.getAllLines().size
        val lineNumbersWidth = lineNumbersRenderer.getWidth(lineCount, fm)

        val adjustedPoint = Point(
            point.x - lineNumbersWidth + scrollModel.scrollX, point.y + scrollModel.scrollY
        )
        return super.getPositionFromPoint(adjustedPoint)
    }

    override fun ensureCaretVisible() {
        if (width == 0 || height == 0) {
            revalidate()
            SwingUtilities.invokeLater {
                ensureCaretVisible()
                repaint()
            }
            return
        }

        scrollModel.maxScrollY = maxOf(0, getContentHeight() - (height - horizontalScrollBar.getWidth()))
        scrollModel.maxScrollX = maxOf(0, getContentWidth() - (width - verticalScrollBar.getWidth()))

        val fm = getFontMetrics(font)
        val lineHeight = fm.height
        val caretPosition = caretModel.getCurrentPosition()
        val caretLine = textBuffer.findLineAt(caretPosition.offset)
        val caretY = lineHeight * textBuffer.getAllLines().indexOfFirst { it.start == caretLine.start } + padding
        val textBeforeCaret = caretLine.text.substring(0, caretPosition.offset - caretLine.start)
        val caretX = fm.stringWidth(textBeforeCaret)

        scrollModel.scrollY = when {
            caretY < scrollModel.scrollY -> caretY
            caretY + lineHeight > scrollModel.scrollY + (height - horizontalScrollBar.getWidth()) -> caretY + lineHeight - (height - horizontalScrollBar.getWidth())

            else -> scrollModel.scrollY
        }.coerceIn(0, scrollModel.maxScrollY)

        scrollModel.scrollX = when {
            caretX < scrollModel.scrollX -> caretX
            caretX > scrollModel.scrollX + (width - verticalScrollBar.getWidth()) -> caretX - (width - verticalScrollBar.getWidth())

            else -> scrollModel.scrollX
        }.coerceIn(0, scrollModel.maxScrollX)
    }

    override fun onTextChanged() {
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        g.font = font
        val originalClip = g.clipBounds

        paintContent(g)
        g.clip = originalClip

        scrollModel.maxScrollY = maxOf(0, getContentHeight() - (height - horizontalScrollBar.getWidth()))
        scrollModel.maxScrollX = maxOf(0, getContentWidth() - (width - verticalScrollBar.getWidth()))

        paintVerticalScrollBar(g)
        paintHorizontalScrollBar(g)
    }

    private fun paintContent(g: Graphics) {
        val clipBounds = g.clipBounds ?: Rectangle(0, 0, width, height)
        val contentClip = Rectangle(clipBounds)
        val fm = g.fontMetrics

        val lineCount = textBuffer.getAllLines().size
        val lineNumbersWidth = if (lineNumbersRenderer.isVisible) {
            lineNumbersRenderer.getWidth(lineCount, fm)
        } else {
            0
        }

        var needsVerticalBar = getContentHeight() > height
        var needsHorizontalBar = getContentWidth() > width

        if (needsVerticalBar) {
            val remainingWidth = width - verticalScrollBar.getWidth()
            needsHorizontalBar = getContentWidth() > remainingWidth
        }

        if (needsHorizontalBar) {
            val remainingHeight = height - horizontalScrollBar.getWidth()
            needsVerticalBar = getContentHeight() > remainingHeight
        }

        contentClip.x = lineNumbersWidth
        contentClip.width = if (needsVerticalBar) {
            width - verticalScrollBar.getWidth() - lineNumbersWidth
        } else {
            width - lineNumbersWidth
        }

        contentClip.height = if (needsHorizontalBar) {
            height - horizontalScrollBar.getWidth()
        } else {
            height
        }

        if (lineNumbersRenderer.isVisible) {
            lineNumbersRenderer.paint(
                g = g,
                lineCount = lineCount,
                firstVisibleLine = scrollModel.scrollY / fm.height,
                visibleLinesCount = contentClip.height / fm.height + 2,
                fm = fm,
                scrollY = scrollModel.scrollY,
                width = lineNumbersWidth
            )
        }

        g.clip = contentClip

        val context = TextRenderer.RenderContext(
            graphics = g,
            clip = clipBounds,
            scrollX = scrollModel.scrollX,
            scrollY = scrollModel.scrollY,
            width = contentClip.width,
            height = contentClip.height,
            caretVisible = caretVisible,
            lineNumbersWidth = lineNumbersWidth
        )

        textRenderer.render(context)
    }

    private fun paintVerticalScrollBar(g: Graphics) {
        val metrics = verticalScrollBar.calculateMetrics(
            viewportSize = height - horizontalScrollBar.getWidth(),
            contentSize = getContentHeight(),
            scroll = scrollModel.scrollY,
            maxScroll = scrollModel.maxScrollY
        )

        verticalScrollBar.paintScrollBar(
            g = g,
            orientation = ScrollBarModel.Orientation.Vertical,
            x = width - verticalScrollBar.getWidth(),
            y = 0,
            length = height - horizontalScrollBar.getWidth(),
            metrics = metrics,
            isHovered = scrollModel.isVerticalHovered,
            isDragging = scrollModel.isVerticalDragging
        )
    }

    private fun paintHorizontalScrollBar(g: Graphics) {
        val metrics = horizontalScrollBar.calculateMetrics(
            viewportSize = width - verticalScrollBar.getWidth(),
            contentSize = getContentWidth(),
            scroll = scrollModel.scrollX,
            maxScroll = scrollModel.maxScrollX
        )

        horizontalScrollBar.paintScrollBar(
            g = g,
            orientation = ScrollBarModel.Orientation.Horizontal,
            x = 0,
            y = height - horizontalScrollBar.getWidth(),
            length = width - verticalScrollBar.getWidth(),
            metrics = metrics,
            isHovered = scrollModel.isHorizontalHovered,
            isDragging = scrollModel.isHorizontalDragging
        )
    }

    private fun getContentHeight(): Int {
        val fm = getFontMetrics(font)
        return fm.height * textBuffer.getAllLines().size + padding * 2
    }

    private fun getContentWidth(): Int {
        val fm = getFontMetrics(font)
        val lineCount = textBuffer.getAllLines().size
        val lineNumbersWidth = if (lineNumbersRenderer.isVisible) {
            lineNumbersRenderer.getWidth(lineCount, fm) + padding * 2
        } else {
            0
        }

        return textBuffer.getAllLines().maxOf { line ->
            fm.stringWidth(line.text)
        } + lineNumbersWidth + padding * 2
    }
}