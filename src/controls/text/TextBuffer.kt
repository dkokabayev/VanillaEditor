package controls.text

import java.io.File

class TextBuffer(private val newLineChar: Char) {
    private val buffer = StringBuilder()
    var caretPosition = 0

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

    fun moveCaretUpWithOption() {
        val currentLine = getCurrentLine()
        val lineStart = currentLine.first

        if (caretPosition > lineStart) {
            caretPosition = lineStart
            return
        }

        if (lineStart > 0) {
            val previousLineEnd = lineStart - 1
            val previousLineStart = buffer.lastIndexOf(newLineChar, previousLineEnd - 1) + 1
            caretPosition = previousLineStart
        }
    }

    fun moveCaretDownWithOption() {
        val currentLine = getCurrentLine()
        val lineStart = currentLine.first
        val lineText = currentLine.second
        val lineEnd = lineStart + lineText.length

        if (caretPosition < lineEnd) {
            caretPosition = lineEnd
            return
        }

        if (lineEnd < buffer.length) {
            val nextLineStart = lineEnd + 1
            val nextLineEnd = buffer.indexOf(newLineChar, nextLineStart).let {
                if (it == -1) buffer.length else it
            }
            caretPosition = nextLineEnd
        }
    }

    val length: Int
        get() = buffer.length

    fun getCurrentLine(): Pair<Int, String> {
        val text = buffer.toString()
        val lineStart = text.lastIndexOf(newLineChar, caretPosition - 1) + 1
        val lineEnd = text.indexOf(newLineChar, caretPosition).let { if (it == -1) text.length else it }
        return Pair(lineStart, text.substring(lineStart, lineEnd))
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