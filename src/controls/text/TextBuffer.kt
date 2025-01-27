package controls.text

class TextBuffer {
    private val buffer = StringBuilder()
    var caretPosition = 0
        private set

    fun insertChar(char: Char) {
        buffer.insert(caretPosition, char)
        caretPosition++
    }

    fun deleteBeforeCaret() {
        if (caretPosition > 0) {
            buffer.deleteCharAt(caretPosition - 1)
            caretPosition--
        }
    }

    fun deleteAtCaret() {
        if (caretPosition < buffer.length) {
            buffer.deleteCharAt(caretPosition)
        }
    }

    fun moveCaretLeft() {
        if (caretPosition > 0) caretPosition--
    }

    fun moveCaretRight() {
        if (caretPosition < buffer.length) caretPosition++
    }

    fun getLines(): List<String> = buffer.toString().split("\n")
}