package controls.text

import java.awt.Color
import java.awt.Graphics
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import kotlin.math.roundToInt

class TextArea(
    fontName: String = "Monospaced",
    fontSize: Int = 14,
    caretBlinkRate: Int = 500,
    repeatInitialDelay: Int = 250,
    repeatAccelerationFactor: Double = 0.8,
    repeatMinDelay: Int = 1,
    fontColor: Color = Color.BLACK,
    selectionColor: Color = Color.PINK,
    newLineChar: Char = '\n',
    private val padding: Int = 5,
    scrollBarWidth: Int = 15,
    scrollBarColor: Color = Color.lightGray,
    scrollBarHoverColor: Color = Color.gray,
    scrollBarDragColor: Color = Color(110, 110, 110),
    scrollBarBackgroundColor: Color = Color(230, 230, 230)
) : TextComponent(
    fontName,
    fontSize,
    caretBlinkRate,
    repeatInitialDelay,
    repeatAccelerationFactor,
    repeatMinDelay,
    newLineChar,
    fontColor,
    selectionColor,
    padding
) {
    private val scrollModel = ScrollModel()
    private val scrollBarModel = ScrollBarModel(
        width = scrollBarWidth,
        color = scrollBarColor,
        hoverColor = scrollBarHoverColor,
        dragColor = scrollBarDragColor,
        backgroundColor = scrollBarBackgroundColor
    )

    init {
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
                if (scrollModel.isDragging) {
                    scrollModel.updateDragPosition(e.y, height, getContentHeight())
                    repaint()
                }
            }
        })
    }

    override fun processMouseEvent(e: MouseEvent) {
        when (e.id) {
            MouseEvent.MOUSE_PRESSED -> {
                if (handleScrollBarClick(e.point)) return
            }

            MouseEvent.MOUSE_RELEASED -> {
                if (scrollModel.isDragging) {
                    scrollModel.stopDragging()
                    repaint()
                    return
                }
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
        if (!scrollBarModel.isInScrollBarArea(point, width, getContentHeight(), height)) {
            return false
        }

        val metrics = scrollBarModel.calculateMetrics(
            componentHeight = height,
            contentHeight = getContentHeight(),
            scrollY = scrollModel.scrollY,
            maxScrollY = scrollModel.maxScrollY
        )

        if (scrollBarModel.isInScrollBarThumb(point, metrics)) {
            scrollModel.startDragging(point.y)
        } else {
            scrollModel.scrollY = scrollBarModel.calculateScrollPositionFromClick(
                clickY = point.y, metrics = metrics, componentHeight = height, maxScrollY = scrollModel.maxScrollY
            )
        }
        repaint()
        return true
    }

    private fun updateScrollBarHover(point: Point) {
        val wasHovered = scrollModel.isHovered
        scrollModel.isHovered = scrollBarModel.isInScrollBarArea(
            point, width, getContentHeight(), height
        )

        if (wasHovered != scrollModel.isHovered) {
            repaint()
        }
    }

    override fun getPositionFromPoint(point: Point): Int {
        val adjustedPoint = Point(point.x, point.y + scrollModel.scrollY)
        return super.getPositionFromPoint(adjustedPoint)
    }

    override fun ensureCaretVisible() {
        val lineHeight = getFontMetrics(font).height
        val caretLine = textBuffer.findLineAt(caretModel.getCurrentPosition().offset)
        val caretY = lineHeight * textBuffer.getAllLines().indexOfFirst { it.start == caretLine.start }

        scrollModel.scrollY = when {
            caretY < scrollModel.scrollY -> caretY
            caretY + lineHeight > scrollModel.scrollY + height -> caretY + lineHeight + padding * 2 - height

            else -> scrollModel.scrollY
        }.coerceIn(0, scrollModel.maxScrollY)
    }

    override fun onTextChanged() {
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        g.font = font
        val originalClip = g.clipBounds
        paintContent(g)
        g.clip = originalClip
        paintScrollBar(g)
    }

    private fun paintContent(g: Graphics) {
        val clipBounds = g.clipBounds ?: Rectangle(0, 0, width, height)
        val contentClip = g.clipBounds?.let { Rectangle(it) } ?: clipBounds
        contentClip.width = width - scrollBarModel.getWidth()
        g.clip = contentClip

        val context = TextRenderer.RenderContext(
            graphics = g,
            clip = clipBounds,
            scrollY = scrollModel.scrollY,
            width = width,
            height = height,
            caretVisible = caretVisible
        )

        textRenderer.render(context)
        g.clip = clipBounds
    }

    private fun paintScrollBar(g: Graphics) {
        scrollModel.maxScrollY = maxOf(0, getContentHeight() - height)

        val metrics = scrollBarModel.calculateMetrics(
            componentHeight = height,
            contentHeight = getContentHeight(),
            scrollY = scrollModel.scrollY,
            maxScrollY = scrollModel.maxScrollY
        )

        scrollBarModel.paint(
            g = g,
            componentWidth = width,
            componentHeight = height,
            metrics = metrics,
            isHovered = scrollModel.isHovered,
            isDragging = scrollModel.isDragging
        )
    }

    private fun getContentHeight(): Int {
        val fm = getFontMetrics(font)
        return fm.height * textBuffer.getAllLines().size + padding * 2
    }
}