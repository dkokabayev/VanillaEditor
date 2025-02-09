package controls.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TextBufferTests {
    private lateinit var buffer: TextBuffer

    @BeforeEach
    fun setUp() {
        buffer = TextBuffer('\n')
    }

    @Test
    fun `empty buffer should have zero length`() {
        assertEquals(0, buffer.length)
        assertEquals("", buffer.getText())
    }

    @Test
    fun `insert single character`() {
        buffer.insertChar('a', 0)
        assertEquals(1, buffer.length)
        assertEquals("a", buffer.getText())
        assertEquals('a', buffer.charAt(0))
    }

    @Test
    fun `insert multiple characters`() {
        buffer.insertChar('a', 0)
        buffer.insertChar('b', 1)
        buffer.insertChar('c', 2)
        assertEquals("abc", buffer.getText())
    }

    @Test
    fun `insert character in middle`() {
        buffer.insertChar('a', 0)
        buffer.insertChar('c', 1)
        buffer.insertChar('b', 1)
        assertEquals("abc", buffer.getText())
    }

    @Test
    fun `delete single character`() {
        buffer.insertChar('a', 0)
        buffer.insertChar('b', 1)
        buffer.deleteCharAt(0)
        assertEquals("b", buffer.getText())
        assertEquals(1, buffer.length)
    }

    @Test
    fun `delete range of characters`() {
        "abcdef".forEach { char ->
            buffer.insertChar(char, buffer.length)
        }

        buffer.deleteRange(1, 4)
        assertEquals("aef", buffer.getText())
    }

    @Test
    fun `find line at position in single line`() {
        "Awesome text".forEach { char ->
            buffer.insertChar(char, buffer.length)
        }

        val line = buffer.findLineAt(5)
        assertEquals(0, line.start)
        assertEquals(12, line.end)
        assertEquals("Awesome text", line.text)
    }

    @Test
    fun `find line at position in multiple lines`() {
        "Awesome\ntext".forEach { char ->
            buffer.insertChar(char, buffer.length)
        }

        val firstLine = buffer.findLineAt(1)
        assertEquals(0, firstLine.start)
        assertEquals(7, firstLine.end)
        assertEquals("Awesome", firstLine.text)

        val secondLine = buffer.findLineAt(9)
        assertEquals(8, secondLine.start)
        assertEquals(12, secondLine.end)
        assertEquals("text", secondLine.text)
    }

    @Test
    fun `get all lines`() {
        "Awesome\ntext".forEach { char ->
            buffer.insertChar(char, buffer.length)
        }

        val lines = buffer.getLines()
        assertEquals(listOf("Awesome", "text"), lines)
    }

    @Test
    fun `find previous and next lines`() {
        "Line1\nLine2\nLine3".forEach { char ->
            buffer.insertChar(char, buffer.length)
        }

        val middleLine = buffer.findLineAt(7)
        val prevLine = buffer.findPreviousLine(middleLine)
        val nextLine = buffer.findNextLine(middleLine)

        assertNotNull(prevLine)
        assertNotNull(nextLine)
        assertEquals("Line1", prevLine?.text)
        assertEquals("Line3", nextLine?.text)
    }

    @Test
    fun `clear buffer`() {
        "Awesome text".forEach { char ->
            buffer.insertChar(char, buffer.length)
        }

        buffer.clear()
        assertEquals(0, buffer.length)
        assertEquals("", buffer.getText())
    }

    @Test
    fun `throw exception when inserting at invalid position`() {
        assertThrows<IllegalArgumentException> {
            buffer.insertChar('a', 1)
        }
        assertThrows<IllegalArgumentException> {
            buffer.insertChar('a', -1)
        }
    }

    @Test
    fun `throw exception when deleting from invalid position`() {
        assertThrows<IllegalArgumentException> {
            buffer.deleteCharAt(0)
        }
        assertThrows<IllegalArgumentException> {
            buffer.deleteCharAt(-1)
        }
    }

    @Test
    fun `throw exception when deleting invalid range`() {
        "Awesome".forEach { char ->
            buffer.insertChar(char, buffer.length)
        }

        assertThrows<IllegalArgumentException> {
            buffer.deleteRange(-1, 3)
        }
        assertThrows<IllegalArgumentException> {
            buffer.deleteRange(3, 2)
        }
        assertThrows<IllegalArgumentException> {
            buffer.deleteRange(0, 8)
        }
    }

    @Test
    fun `line cache updates correctly after modifications`() {
        "Line1\nLine2\nLine3".forEach { char ->
            buffer.insertChar(char, buffer.length)
        }

        val initialLines = buffer.getLines()
        assertEquals(3, initialLines.size)

        buffer.insertChar('B', 6)
        buffer.insertChar('e', 7)
        buffer.insertChar('e', 8)
        val modifiedLines = buffer.getLines()
        assertEquals(listOf("Line1", "BeeLine2", "Line3"), modifiedLines)

        buffer.deleteCharAt(13)
        val finalLines = buffer.getLines()
        assertEquals(listOf("Line1", "BeeLine", "Line3"), finalLines)
    }
}