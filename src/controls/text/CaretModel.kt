package controls.text

class CaretModel(private val textBuffer: TextBuffer) {
    var position: Int = 0
        private set

    fun getCurrentPosition(): CaretPosition {
        val lineInfo = textBuffer.findLineAt(position)
        return CaretPosition(
            offset = position,
            start = lineInfo.start,
            end = lineInfo.end,
            text = lineInfo.text
        )
    }

    fun moveCursorUpPreserveColumn() {
        val currentLine = textBuffer.findLineAt(position)
        val columnOffset = position - currentLine.start

        textBuffer.findPreviousLine(currentLine)?.let { prevLine ->
            position = prevLine.start + minOf(columnOffset, prevLine.end - prevLine.start)
        }
    }

    fun moveCursorDownPreserveColumn() {
        val currentLine = textBuffer.findLineAt(position)
        val columnOffset = position - currentLine.start

        textBuffer.findNextLine(currentLine)?.let { nextLine ->
            position = nextLine.start + minOf(columnOffset, nextLine.end - nextLine.start)
        }
    }

    fun moveTo(newPosition: Int) {
        position = newPosition
    }

    fun moveLeft() {
        if (position > 0) position--
    }

    fun moveRight() {
        if (position < textBuffer.length) position++
    }

    fun moveToPreviousWord() {
        if (position <= 0) return

        var newPosition = position - 1

        if (newPosition > 0 && textBuffer.charAt(newPosition) == textBuffer.newLineChar) {
            newPosition--
            position = newPosition + 1
            return
        }

        while (newPosition > 0 && textBuffer.charAt(newPosition).isWhitespace()) {
            if (textBuffer.charAt(newPosition) == textBuffer.newLineChar) {
                position = newPosition + 1
                return
            }
            newPosition--
        }

        while (newPosition > 0 && !textBuffer.charAt(newPosition - 1).isWhitespace()) {
            newPosition--
        }

        position = newPosition
    }

    fun moveToNextWord() {
        if (position >= textBuffer.length) return

        var newPosition = position

        val isAtWordEnd = newPosition == textBuffer.length ||
                textBuffer.charAt(newPosition).isWhitespace() ||
                (newPosition > 0 && !textBuffer.charAt(newPosition - 1).isWhitespace() &&
                        textBuffer.charAt(newPosition).isWhitespace())

        if (!isAtWordEnd) {
            while (newPosition < textBuffer.length && !textBuffer.charAt(newPosition).isWhitespace()) {
                newPosition++
            }
        } else {
            while (newPosition < textBuffer.length && textBuffer.charAt(newPosition).isWhitespace()) {
                if (textBuffer.charAt(newPosition) == textBuffer.newLineChar) {
                    newPosition++
                    position = newPosition
                    return
                }
                newPosition++
            }

            while (newPosition < textBuffer.length && !textBuffer.charAt(newPosition).isWhitespace()) {
                newPosition++
            }
        }

        position = newPosition
    }

    fun moveToLineStart() {
        position = textBuffer.findLineAt(position).start
    }

    fun moveToPreviousLineStart() {
        textBuffer.findPreviousLine(textBuffer.findLineAt(position))?.let {
            position = it.start
        }
    }

    fun moveToLineEnd() {
        position = textBuffer.findLineAt(position).end
    }

    fun moveToNextLineEnd() {
        textBuffer.findNextLine(textBuffer.findLineAt(position))?.let {
            position = it.end
        }
    }

    fun moveToTextStart() {
        position = 0
    }

    fun moveToTextEnd() {
        position = textBuffer.length
    }
}
