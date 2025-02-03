package controls.text

internal class SelectionModel(private val textBuffer: TextBuffer) {
    var selectionStart: Int = -1
        private set
    var selectionEnd: Int = -1
        private set

    val hasSelection: Boolean
        get() = selectionStart != -1 && selectionEnd != -1

    data class SelectionBounds(
        val start: Int, val end: Int, val text: String
    )

    fun getCurrentSelection(): SelectionBounds? {
        if (!hasSelection) return null
        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)
        return SelectionBounds(
            start = start, end = end, text = textBuffer.getText().substring(start, end)
        )
    }

    fun startSelection(position: Int) {
        selectionStart = position.coerceIn(0, textBuffer.length)
        selectionEnd = selectionStart
    }

    fun updateSelection(position: Int) {
        if (selectionStart != -1) {
            selectionEnd = position.coerceIn(0, textBuffer.length)
        } else {
            startSelection(position)
        }
    }

    fun clearSelection() {
        selectionStart = -1
        selectionEnd = -1
    }

    fun getSelectedText(): String = getCurrentSelection()?.text ?: ""

    fun selectWord(position: Int): Pair<Int, Int> {
        val text = textBuffer.getText()
        if (text.isEmpty()) {
            return Pair(0, 0).also {
                startSelection(it.first)
                updateSelection(it.second)
            }
        }

        var wordStart = position
        while (wordStart > 0 && !text[wordStart - 1].isWhitespace()) {
            wordStart--
        }

        var wordEnd = position
        while (wordEnd < text.length && !text[wordEnd].isWhitespace()) {
            wordEnd++
        }

        return Pair(wordStart, wordEnd).also {
            startSelection(it.first)
            updateSelection(it.second)
        }
    }

    fun selectLine(lineStart: Int, lineEnd: Int) {
        startSelection(lineStart)
        updateSelection(lineEnd)
    }
}