package controls.text

import java.awt.Color
import java.awt.Graphics
import kotlin.math.roundToInt

internal class ScrollBarModel(
    private val width: Int,
    private val color: Color,
    private val hoverColor: Color,
    private val dragColor: Color,
    private val backgroundColor: Color
) {
    data class Metrics(val thumbSize: Int, val thumbPosition: Int, val isVisible: Boolean)

    enum class Orientation {
        Vertical, Horizontal
    }

    fun calculateMetrics(viewportSize: Int, contentSize: Int, scroll: Int, maxScroll: Int): Metrics {
        if (contentSize <= viewportSize) {
            return Metrics(thumbSize = 0, thumbPosition = 0, isVisible = false)
        }

        val thumbSize = maxOf(
            (viewportSize.toFloat() * viewportSize / contentSize).roundToInt(), width
        )
        val maxThumbPosition = viewportSize - thumbSize

        val thumbPosition = when {
            maxThumbPosition <= 0 -> 0
            maxScroll <= 0 -> 0
            else -> (scroll.toFloat() * maxThumbPosition / maxScroll).roundToInt()
        }

        return Metrics(
            thumbSize = thumbSize, thumbPosition = thumbPosition, isVisible = true
        )
    }

    fun calculateScrollPositionFromClick(
        click: Int, metrics: Metrics, viewportSize: Int, maxScroll: Int
    ): Int {
        val clickPercent = (click - metrics.thumbSize / 2).toFloat() / (viewportSize - metrics.thumbSize)
        return (clickPercent * maxScroll).roundToInt().coerceIn(0, maxScroll)
    }

    fun paintScrollBar(
        g: Graphics,
        orientation: Orientation,
        x: Int,
        y: Int,
        length: Int,
        metrics: Metrics,
        isHovered: Boolean,
        isDragging: Boolean
    ) {
        if (!metrics.isVisible) return

        g.color = backgroundColor
        when (orientation) {
            Orientation.Vertical -> g.fillRect(x, y, width, length)
            Orientation.Horizontal -> g.fillRect(x, y, length, width)
        }

        g.color = when {
            isDragging -> dragColor
            isHovered -> hoverColor
            else -> color
        }

        val padding = 2
        when (orientation) {
            Orientation.Vertical -> g.fillRect(
                x + padding, y + metrics.thumbPosition, width - 2 * padding, metrics.thumbSize
            )

            Orientation.Horizontal -> g.fillRect(
                x + metrics.thumbPosition, y + padding, metrics.thumbSize, width - 2 * padding
            )
        }
    }

    fun getWidth(): Int = width
}