package controls.text

import java.awt.Color
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Rectangle

internal class TextRenderer(
    private val textBuffer: TextBuffer,
    private val caretModel: CaretModel,
    private val selectionModel: SelectionModel,
    private val padding: Int,
    private val fontColor: Color,
    private val selectionColor: Color
) {
    data class RenderContext(
        val graphics: Graphics,
        val clip: Rectangle?,
        val scrollY: Int = 0,
        val width: Int = 0,
        val height: Int = 0,
        val caretVisible: Boolean = true
    )

    data class VisibleContent(
        val firstVisibleLine: Int,
        val visibleLines: List<TextBuffer.LineInfo>,
        val lineHeight: Int,
        val fontMetrics: FontMetrics
    )

    private fun calculateVisibleContent(
        g: Graphics,
        scrollY: Int,
        height: Int
    ): VisibleContent {
        val fm = g.fontMetrics
        val lineHeight = fm.height

        if (scrollY == 0) {
            return VisibleContent(0, textBuffer.getAllLines(), lineHeight, fm)
        }

        val firstVisibleLine = (scrollY / lineHeight).coerceAtLeast(0)
        val visibleLinesCount = (height / lineHeight + 2)

        val allLines = textBuffer.getAllLines()
        val visibleLines = allLines.subList(
            firstVisibleLine.coerceAtMost(allLines.size),
            (firstVisibleLine + visibleLinesCount).coerceAtMost(allLines.size)
        )

        return VisibleContent(firstVisibleLine, visibleLines, lineHeight, fm)
    }

    fun render(context: RenderContext) {
        val visibleContent = calculateVisibleContent(
            context.graphics,
            context.scrollY,
            context.height
        )

        if (context.scrollY != 0) {
            context.graphics.translate(0, -context.scrollY)
        }

        renderContent(context, visibleContent)

        if (context.scrollY != 0) {
            context.graphics.translate(0, context.scrollY)
        }
    }

    private fun renderContent(
        context: RenderContext,
        visibleContent: VisibleContent
    ) {
        val g = context.graphics
        val (firstVisibleLine, visibleLines, lineHeight, fm) = visibleContent

        selectionModel.getCurrentSelection()?.let { selection ->
            g.color = selectionColor
            renderSelection(g, visibleContent, selection.start, selection.end)
        }

        g.color = fontColor
        var y = (firstVisibleLine * lineHeight) + fm.ascent + padding
        for (line in visibleLines) {
            g.drawString(line.text, padding, y)
            y += lineHeight
        }

        if (context.caretVisible) {
            renderCaret(g, visibleContent)
        }
    }

    private fun renderSelection(
        g: Graphics,
        visibleContent: VisibleContent,
        selectionStart: Int,
        selectionEnd: Int
    ) {
        val (firstVisibleLine, visibleLines, lineHeight, fm) = visibleContent
        var y = (firstVisibleLine * lineHeight) + padding

        for (line in visibleLines) {
            val lineStart = line.start
            val lineEnd = line.end

            if (selectionEnd > lineStart && selectionStart < lineEnd + 1) {
                val selStart = maxOf(selectionStart - lineStart, 0)
                val selEnd = minOf(selectionEnd - lineStart, line.text.length)

                if (line.text.isEmpty() && selStart == 0) {
                    g.fillRect(padding, y, fm.charWidth(' '), lineHeight)
                } else {
                    val startX = fm.stringWidth(line.text.substring(0, selStart))
                    val width = fm.stringWidth(line.text.substring(selStart, selEnd))
                    g.fillRect(startX + padding, y, width, lineHeight)
                }
            }
            y += lineHeight
        }
    }

    private fun renderCaret(
        g: Graphics,
        visibleContent: VisibleContent
    ) {
        val (firstVisibleLine, visibleLines, lineHeight, fm) = visibleContent
        val caretPosition = caretModel.getCurrentPosition()
        val caretLine = textBuffer.findLineAt(caretPosition.offset)

        val lineIndex = textBuffer.getAllLines().indexOfFirst { it.start == caretLine.start }
        if (lineIndex in firstVisibleLine..<firstVisibleLine + visibleLines.size) {
            val textBeforeCaret = caretLine.text.substring(0, caretPosition.offset - caretLine.start)
            val caretX = fm.stringWidth(textBeforeCaret)
            val caretY = (lineIndex * lineHeight) + padding + fm.ascent

            g.drawLine(
                caretX + padding,
                caretY - fm.ascent,
                caretX + padding,
                caretY - fm.ascent + lineHeight
            )
        }
    }
}