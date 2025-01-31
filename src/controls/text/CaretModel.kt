package controls.text

internal class CaretModel(private val textBuffer: TextBuffer) {
    var position: Int = 0
        private set

    fun getCurrentPosition(): CaretPosition {
        val text = textBuffer.getText()
        val lineStart = text.lastIndexOf(textBuffer.newLineChar, position - 1) + 1
        val lineEnd = text.indexOf(textBuffer.newLineChar, position).let {
            if (it == -1) text.length else it
        }

        return CaretPosition(
            offset = position,
            lineStart = lineStart,
            lineEnd = lineEnd,
            lineText = text.substring(lineStart, lineEnd)
        )
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
        position = getCurrentPosition().lineStart
    }

    fun moveToLineEnd() {
        position = getCurrentPosition().lineEnd
    }

    fun moveToTextStart() {
        position = 0
    }

    fun moveToTextEnd() {
        position = textBuffer.length
    }

    fun moveUpWithOption() {
        val currentPos = getCurrentPosition()

        if (position > currentPos.lineStart) {
            position = currentPos.lineStart
            return
        }

        if (currentPos.lineStart > 0) {
            val previousLineEnd = currentPos.lineStart - 1
            val previousLineStart = textBuffer.getText().lastIndexOf(textBuffer.newLineChar, previousLineEnd - 1) + 1
            position = previousLineStart
        }
    }

    fun moveDownWithOption() {
        val currentPos = getCurrentPosition()

        if (position < currentPos.lineEnd) {
            position = currentPos.lineEnd
            return
        }

        if (currentPos.lineEnd < textBuffer.length) {
            val nextLineStart = currentPos.lineEnd + 1
            val nextLineEnd = textBuffer.getText().indexOf(textBuffer.newLineChar, nextLineStart).let {
                if (it == -1) textBuffer.length else it
            }
            position = nextLineEnd
        }
    }
}

