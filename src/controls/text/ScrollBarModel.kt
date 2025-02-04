package controls.text

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import kotlin.math.roundToInt

internal class ScrollBarModel(
    private val width: Int,
    internal var color: Color,
    internal var hoverColor: Color,
    internal var dragColor: Color,
    internal var backgroundColor: Color,
    private val cornerRadius: Int
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

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2d.color = backgroundColor
        val archWidth = (cornerRadius * 2).toFloat()
        val backgroundShape = when (orientation) {
            Orientation.Vertical -> RoundRectangle2D.Float(
                x.toFloat(),
                y.toFloat(),
                width.toFloat(),
                length.toFloat(),
                archWidth,
                archWidth
            )

            Orientation.Horizontal -> RoundRectangle2D.Float(
                x.toFloat(),
                y.toFloat(),
                length.toFloat(),
                width.toFloat(),
                archWidth,
                archWidth
            )
        }
        g2d.fill(backgroundShape)

        g2d.color = when {
            isDragging -> dragColor
            isHovered -> hoverColor
            else -> color
        }

        val padding = 2
        val thumbArchWidth = ((cornerRadius - padding) * 2).toFloat()

        val thumbShape = when (orientation) {
            Orientation.Vertical -> RoundRectangle2D.Float(
                (x + padding).toFloat(),
                (y + metrics.thumbPosition).toFloat(),
                (width - 2 * padding).toFloat(),
                metrics.thumbSize.toFloat(),
                thumbArchWidth,
                thumbArchWidth
            )

            Orientation.Horizontal -> RoundRectangle2D.Float(
                (x + metrics.thumbPosition).toFloat(),
                (y + padding).toFloat(),
                metrics.thumbSize.toFloat(),
                (width - 2 * padding).toFloat(),
                thumbArchWidth,
                thumbArchWidth
            )
        }
        g2d.fill(thumbShape)
    }

    fun getWidth(): Int = width
}