package controls.text

import java.awt.Color
import java.awt.Graphics
import java.awt.Point
import kotlin.math.roundToInt

class ScrollBarModel(
    private val width: Int = 15,
    private val color: Color = Color.lightGray,
    private val hoverColor: Color = Color.gray,
    private val dragColor: Color = Color(110, 110, 110),
    private val backgroundColor: Color = Color(230, 230, 230)
) {
    data class Metrics(
        val height: Int,
        val y: Int,
        val isVisible: Boolean
    )

    fun calculateMetrics(
        componentHeight: Int,
        contentHeight: Int,
        scrollY: Int,
        maxScrollY: Int
    ): Metrics {
        val calculatedScrollBarHeight = (componentHeight.toFloat() * componentHeight / contentHeight).roundToInt()
        val scrollBarHeight = maxOf(calculatedScrollBarHeight, width)
        val maxScrollBarY = componentHeight - scrollBarHeight

        val scrollBarY = if (maxScrollBarY <= 0) {
            0
        } else {
            (scrollY.toFloat() * maxScrollBarY / maxScrollY).roundToInt()
        }

        return Metrics(
            height = scrollBarHeight,
            y = scrollBarY,
            isVisible = contentHeight > componentHeight
        )
    }

    fun isInScrollBarArea(
        point: Point,
        componentWidth: Int,
        contentHeight: Int,
        componentHeight: Int
    ): Boolean {
        return point.x >= componentWidth - width && contentHeight > componentHeight
    }

    fun isInScrollBarThumb(point: Point, metrics: Metrics): Boolean {
        return point.y >= metrics.y && point.y <= metrics.y + metrics.height
    }

    fun calculateScrollPositionFromClick(
        clickY: Int,
        metrics: Metrics,
        componentHeight: Int,
        maxScrollY: Int
    ): Int {
        val clickPercent = (clickY - metrics.height / 2).toFloat() / (componentHeight - metrics.height)
        return (clickPercent * maxScrollY).roundToInt().coerceIn(0, maxScrollY)
    }

    fun paint(
        g: Graphics,
        componentWidth: Int,
        componentHeight: Int,
        metrics: Metrics,
        isHovered: Boolean,
        isDragging: Boolean
    ) {
        if (!metrics.isVisible) return

        g.color = backgroundColor
        g.fillRect(componentWidth - width, 0, width, componentHeight)

        g.color = when {
            isDragging -> dragColor
            isHovered -> hoverColor
            else -> color
        }
        g.fillRect(
            componentWidth - width + 2,
            metrics.y,
            width - 4,
            metrics.height
        )
    }

    fun getWidth(): Int = width
}