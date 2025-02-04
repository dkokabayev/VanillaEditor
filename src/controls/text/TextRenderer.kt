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
    internal var foregroundColor: Color,
    internal var selectionColor: Color,
    private val caretWidth: Int,
    internal var caretColor: Color,
) {
    data class RenderContext(
        val graphics: Graphics,
        val clip: Rectangle?,
        val scrollX: Int = 0,
        val scrollY: Int = 0,
        val width: Int = 0,
        val height: Int = 0,
        val caretVisible: Boolean = true,
        val lineNumbersWidth: Int = 0
    )

    data class VisibleContent(
        val firstVisibleLine: Int,
        val visibleLines: List<TextBuffer.LineInfo>,
        val lineHeight: Int,
        val fontMetrics: FontMetrics,
        val visibleCharRanges: List<CharRange>
    )

    internal data class CharRange(
        val start: Int, val end: Int
    )

    private fun calculateVisibleContent(
        g: Graphics, scrollX: Int, scrollY: Int, viewportWidth: Int, viewportHeight: Int
    ): VisibleContent {
        val fm = g.fontMetrics
        val lineHeight = fm.height

        val firstVisibleLine = (scrollY / lineHeight).coerceAtLeast(0)
        val visibleLinesCount = (viewportHeight / lineHeight + 2)

        val allLines = textBuffer.getAllLines()
        val visibleLines = allLines.subList(
            firstVisibleLine.coerceAtMost(allLines.size),
            (firstVisibleLine + visibleLinesCount).coerceAtMost(allLines.size)
        )

        val visibleCharRanges = visibleLines.map { line ->
            calculateVisibleCharRange(line.text, fm, scrollX, viewportWidth)
        }

        return VisibleContent(
            firstVisibleLine = firstVisibleLine,
            visibleLines = visibleLines,
            lineHeight = lineHeight,
            fontMetrics = fm,
            visibleCharRanges = visibleCharRanges
        )
    }

    private fun calculateVisibleCharRange(
        text: String, fm: FontMetrics, scrollX: Int, viewportWidth: Int
    ): CharRange {
        if (text.isEmpty()) return CharRange(0, 0)

        var currentWidth = 0
        val startChar = text.indices.firstOrNull { index ->
            currentWidth += fm.charWidth(text[index])
            currentWidth > scrollX
        } ?: 0

        currentWidth = 0
        val endChar = (startChar..text.length).firstOrNull { index ->
            if (index < text.length) {
                currentWidth += fm.charWidth(text[index])
                currentWidth > viewportWidth
            } else true
        } ?: text.length

        return CharRange(startChar, endChar)
    }

    fun render(context: RenderContext) {
        val visibleContent = calculateVisibleContent(
            g = context.graphics,
            scrollX = context.scrollX,
            scrollY = context.scrollY,
            viewportWidth = context.width,
            viewportHeight = context.height
        )

        context.graphics.translate(
            -context.scrollX + context.lineNumbersWidth, -context.scrollY
        )
        renderContent(context, visibleContent)
        context.graphics.translate(
            context.scrollX - context.lineNumbersWidth, context.scrollY
        )
    }

    private fun renderContent(
        context: RenderContext, visibleContent: VisibleContent
    ) {
        val g = context.graphics
        val (firstVisibleLine, visibleLines, lineHeight, fm, visibleCharRanges) = visibleContent

        selectionModel.getCurrentSelection()?.let { selection ->
            g.color = selectionColor
            renderSelection(g, visibleContent, selection.start, selection.end)
        }

        g.color = foregroundColor
        var y = (firstVisibleLine * lineHeight) + fm.ascent + padding

        for ((line, charRange) in visibleLines.zip(visibleCharRanges)) {
            if (line.text.isNotEmpty() && charRange.end > charRange.start) {
                var x = padding
                if (charRange.start > 0) {
                    x += fm.stringWidth(line.text.substring(0, charRange.start))
                }

                val visibleText = line.text.substring(charRange.start, charRange.end)
                g.drawString(visibleText, x, y)
            }
            y += lineHeight
        }

        if (context.caretVisible) {
            renderCaret(g, visibleContent)
        }
    }

    private fun renderSelection(
        g: Graphics, visibleContent: VisibleContent, selectionStart: Int, selectionEnd: Int
    ) {
        val (firstVisibleLine, visibleLines, lineHeight, fm, visibleCharRanges) = visibleContent
        var y = (firstVisibleLine * lineHeight) + padding

        for ((line, charRange) in visibleLines.zip(visibleCharRanges)) {
            val lineStart = line.start
            val lineEnd = line.end

            if (selectionEnd > lineStart && selectionStart < lineEnd + 1) {
                val visibleSelStart = maxOf(selectionStart - lineStart, charRange.start)
                val visibleSelEnd = minOf(selectionEnd - lineStart, charRange.end)

                if (visibleSelEnd > visibleSelStart) {
                    val startX = if (visibleSelStart > 0) {
                        fm.stringWidth(line.text.substring(0, visibleSelStart))
                    } else 0

                    val width = fm.stringWidth(
                        line.text.substring(
                            visibleSelStart, visibleSelEnd
                        )
                    )

                    g.fillRect(startX + padding, y, width, lineHeight)
                }
            }
            y += lineHeight
        }
    }

    private fun renderCaret(g: Graphics, visibleContent: VisibleContent) {
        val (firstVisibleLine, visibleLines, lineHeight, fm, _) = visibleContent
        val caretPosition = caretModel.getCurrentPosition()
        val caretLine = textBuffer.findLineAt(caretPosition.offset)

        val lineIndex = textBuffer.getAllLines().indexOfFirst { it.start == caretLine.start }
        if (lineIndex in firstVisibleLine..<firstVisibleLine + visibleLines.size) {
            val textBeforeCaret = caretLine.text.substring(0, caretPosition.offset - caretLine.start)
            val caretX = fm.stringWidth(textBeforeCaret)
            val caretY = (lineIndex * lineHeight) + padding + fm.ascent

            g.color = caretColor
            g.fillRect(
                caretX + padding - caretWidth / 2, caretY - fm.ascent, caretWidth, lineHeight
            )
        }
    }
}