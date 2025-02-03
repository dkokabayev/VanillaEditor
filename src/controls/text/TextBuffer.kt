package controls.text

import java.util.*

internal class TextBuffer(val newLineChar: Char) {
    private val buffer = StringBuilder()
    private val lineCache = TreeMap<Int, LineInfo>()
    private var lastKnownTextLength = 0

    val length: Int
        get() = buffer.length

    data class LineInfo(
        val start: Int, val end: Int, val text: String
    )

    private fun scanLines(startPos: Int, onLineFound: (start: Int, end: Int, text: String) -> Unit) {
        var currentLineStart = startPos
        var currentPos = startPos

        while (currentPos < buffer.length) {
            if (buffer[currentPos] == newLineChar) {
                val lineText = buffer.substring(currentLineStart, currentPos)
                onLineFound(currentLineStart, currentPos, lineText)
                currentLineStart = currentPos + 1
            }
            currentPos++
        }

        if (currentLineStart <= buffer.length) {
            val lineText = buffer.substring(currentLineStart, buffer.length)
            onLineFound(currentLineStart, buffer.length, lineText)
        }
    }

    private fun updateLineCache() {
        if (buffer.length == lastKnownTextLength) return

        lineCache.clear()
        if (buffer.isEmpty()) {
            lineCache[0] = LineInfo(0, 0, "")
        } else {
            scanLines(0) { start, end, text ->
                lineCache[start] = LineInfo(start, end, text)
            }
        }
        lastKnownTextLength = buffer.length
    }

    private fun updateLineCacheIncrementally(changePosition: Int) {
        if (lineCache.isEmpty() || changePosition == 0) {
            updateLineCache()
            return
        }

        val affectedEntry = lineCache.floorEntry(changePosition)
        if (affectedEntry != null) {
            lineCache.tailMap(affectedEntry.key).clear()

            scanLines(affectedEntry.key) { start, end, text ->
                lineCache[start] = LineInfo(start, end, text)
            }
        } else {
            updateLineCache()
        }

        lastKnownTextLength = buffer.length
    }

    fun findLineAt(position: Int): LineInfo {
        updateLineCache()
        val pos = position.coerceIn(0, length)
        return lineCache.floorEntry(pos)?.value ?: lineCache.firstEntry()?.value ?: LineInfo(0, 0, "")
    }

    fun findPreviousLine(currentLine: LineInfo): LineInfo? {
        updateLineCache()
        return lineCache.lowerEntry(currentLine.start)?.value
    }

    fun findNextLine(currentLine: LineInfo): LineInfo? {
        updateLineCache()
        return lineCache.higherEntry(currentLine.start)?.value
    }

    fun getAllLines(): List<LineInfo> {
        updateLineCache()
        return lineCache.values.toList()
    }

    fun getText(): String = buffer.toString()

    fun charAt(index: Int): Char = buffer[index]

    fun insertChar(char: Char, position: Int) {
        require(position in 0..buffer.length) {
            "Position $position is out of bounds (0..${buffer.length})"
        }
        buffer.insert(position, char)
        updateLineCacheIncrementally(position)
    }

    fun deleteCharAt(position: Int) {
        require(position in buffer.indices) {
            "Position $position is out of bounds (0..${buffer.length - 1})"
        }
        buffer.deleteCharAt(position)
        updateLineCacheIncrementally(position)
    }

    fun clear() {
        buffer.setLength(0)
        lineCache.clear()
        lastKnownTextLength = 0
        lineCache[0] = LineInfo(0, 0, "")
    }

    fun getLines(): List<String> = getAllLines().map { it.text }
}