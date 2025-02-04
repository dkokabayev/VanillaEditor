package controls.text

import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics

internal class LineNumbersRenderer(
    var isVisible: Boolean,
    private val padding: Int,
    private val font: Font,
    private val color: Color,
    private val backgroundColor: Color
) {
    fun getWidth(lineCount: Int, fm: FontMetrics): Int {
        if (!isVisible) return 0
        return fm.stringWidth(lineCount.toString()) + padding * 2
    }

    fun paint(
        g: Graphics,
        lineCount: Int,
        firstVisibleLine: Int,
        visibleLinesCount: Int,
        fm: FontMetrics,
        scrollY: Int,
        width: Int
    ) {
        if (!isVisible) return

        g.color = backgroundColor
        g.fillRect(0, 0, width, g.clipBounds.height)

        g.color = color
        g.font = font

        val lineHeight = fm.height
        val maxLines = minOf(firstVisibleLine + visibleLinesCount, lineCount)

        for (i in firstVisibleLine..<maxLines) {
            val lineNumber = (i + 1).toString()
            val x = width - fm.stringWidth(lineNumber) - padding
            val y = i * lineHeight + fm.ascent - scrollY + padding
            g.drawString(lineNumber, x, y)
        }
    }
}