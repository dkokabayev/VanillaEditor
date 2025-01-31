package controls.text

import java.io.File

internal class TextBuffer(val newLineChar: Char) {
    private val buffer = StringBuilder()

    val length: Int
        get() = buffer.length

    fun getText(): String = buffer.toString()

    fun charAt(index: Int): Char = buffer[index]

    fun insertChar(char: Char, position: Int) {
        buffer.insert(position, char)
    }

    fun deleteCharAt(position: Int) {
        buffer.deleteCharAt(position)
    }

    fun clear() {
        buffer.setLength(0)
    }

    fun getLines(): List<String> = buffer.toString().split(newLineChar)
}