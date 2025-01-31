package controls.text

internal class SelectionModel(private val textBuffer: TextBuffer) {
    var selectionStart: Int = -1
        private set
    var selectionEnd: Int = -1
        private set

    val hasSelection: Boolean
        get() = selectionStart != -1 && selectionEnd != -1

    fun startSelection(position: Int) {
        selectionStart = position
        selectionEnd = position
    }

    fun updateSelection(position: Int) {
        if (selectionStart != -1) {
            selectionEnd = position.coerceIn(0, textBuffer.length)
        }
    }

    fun clearSelection() {
        selectionStart = -1
        selectionEnd = -1
    }

    fun getSelectedText(): String {
        if (!hasSelection) return ""
        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)
        return textBuffer.getText().substring(start, end)
    }

    fun getSelectionBounds(): Pair<Int, Int>? {
        if (!hasSelection) return null
        return Pair(
            minOf(selectionStart, selectionEnd),
            maxOf(selectionStart, selectionEnd)
        )
    }
}