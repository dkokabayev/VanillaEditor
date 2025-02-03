package controls.text

internal class TextBuffer(val newLineChar: Char) {
    private val buffer = StringBuilder()
    private val lineCache = mutableListOf<LineInfo>()
    private var lastKnownTextLength = 0

    val length: Int
        get() = buffer.length

    data class LineInfo(
        val start: Int,
        val end: Int,
        val text: String
    )

    private fun updateLineCache() {
        if (buffer.length == lastKnownTextLength) return

        lineCache.clear()
        var currentLineStart = 0
        var currentPos = 0

        while (currentPos < buffer.length) {
            if (buffer[currentPos] == newLineChar) {
                val lineText = buffer.substring(currentLineStart, currentPos)
                lineCache.add(LineInfo(currentLineStart, currentPos, lineText))
                currentLineStart = currentPos + 1
            }
            currentPos++
        }

        if (currentLineStart <= buffer.length) {
            val lineText = buffer.substring(currentLineStart, buffer.length)
            lineCache.add(LineInfo(currentLineStart, buffer.length, lineText))
        }

        lastKnownTextLength = buffer.length
    }

    fun findLineAt(position: Int): LineInfo {
        updateLineCache()

        val pos = position.coerceIn(0, length)
        return lineCache.find { pos in it.start..it.end }
            ?: lineCache.lastOrNull()
            ?: LineInfo(0, 0, "")
    }

    fun findPreviousLine(currentLine: LineInfo): LineInfo? {
        updateLineCache()
        val currentIndex = lineCache.indexOfFirst { it.start == currentLine.start }
        if (currentIndex <= 0) return null
        return lineCache[currentIndex - 1]
    }

    fun findNextLine(currentLine: LineInfo): LineInfo? {
        updateLineCache()
        val currentIndex = lineCache.indexOfFirst { it.start == currentLine.start }
        if (currentIndex == -1 || currentIndex >= lineCache.size - 1) return null
        return lineCache[currentIndex + 1]
    }

    fun getAllLines(): List<LineInfo> {
        updateLineCache()
        return lineCache.toList()
    }

    fun getText(): String = buffer.toString()

    fun charAt(index: Int): Char = buffer[index]

    fun insertChar(char: Char, position: Int) {
        buffer.insert(position, char)
        lastKnownTextLength = -1
    }

    fun deleteCharAt(position: Int) {
        buffer.deleteCharAt(position)
        lastKnownTextLength = -1
    }

    fun clear() {
        buffer.setLength(0)
        lineCache.clear()
        lastKnownTextLength = 0
    }

    fun getLines(): List<String> = getAllLines().map { it.text }
}