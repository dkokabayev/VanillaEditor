package controls.text

import java.io.File

class TextBuffer(private val newLineChar: Char) {
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

    fun moveCaretToPreviousWord() {
        if (caretPosition <= 0) return

        var newPosition = caretPosition - 1

        if (newPosition > 0 && buffer[newPosition] == newLineChar) {
            newPosition--
            caretPosition = newPosition + 1
            return
        }

        while (newPosition > 0 && buffer[newPosition].isWhitespace()) {
            if (buffer[newPosition] == newLineChar) {
                caretPosition = newPosition + 1
                return
            }
            newPosition--
        }

        while (newPosition > 0 && !buffer[newPosition - 1].isWhitespace()) {
            newPosition--
        }

        caretPosition = newPosition
    }

    fun moveCaretToNextWord() {
        if (caretPosition >= buffer.length) return

        var newPosition = caretPosition

        val isAtWordEnd = newPosition == buffer.length ||
                buffer[newPosition].isWhitespace() ||
                (newPosition > 0 && !buffer[newPosition - 1].isWhitespace() && buffer[newPosition].isWhitespace())

        if (!isAtWordEnd) {
            while (newPosition < buffer.length && !buffer[newPosition].isWhitespace()) {
                newPosition++
            }
        } else {
            while (newPosition < buffer.length && buffer[newPosition].isWhitespace()) {
                if (buffer[newPosition] == newLineChar) {
                    newPosition++
                    caretPosition = newPosition
                    return
                }
                newPosition++
            }

            while (newPosition < buffer.length && !buffer[newPosition].isWhitespace()) {
                newPosition++
            }
        }

        caretPosition = newPosition
    }

    fun getLines(): List<String> = buffer.toString().split("\n")

    fun clear() {
        buffer.setLength(0)
        caretPosition = 0
    }

    fun loadFromFile(file: File) {
        clear()
        buffer.append(file.readText())
        caretPosition = buffer.length
    }

    fun saveToFile(file: File) {
        file.writeText(buffer.toString())
    }
}