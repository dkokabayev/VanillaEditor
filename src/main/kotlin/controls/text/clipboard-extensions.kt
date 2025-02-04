package controls.text

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

private val systemClipboard = Toolkit.getDefaultToolkit().systemClipboard

internal fun KeyEvent.handleClipboardCopy(
    selectionModel: SelectionModel
): Boolean {
    if (!isClipboardShortcut()) return false
    if (!selectionModel.hasSelection) return false

    val selectedText = selectionModel.getSelectedText()
    val stringSelection = StringSelection(selectedText)
    systemClipboard.setContents(stringSelection, null)
    return true
}

internal fun KeyEvent.handleClipboardCut(
    selectionModel: SelectionModel, textBuffer: TextBuffer, caretModel: CaretModel, undoManager: UndoManager
): Boolean {
    if (!isClipboardShortcut()) return false
    if (!selectionModel.hasSelection) return false

    handleClipboardCopy(selectionModel)
    selectionModel.deleteSelectedText(textBuffer, caretModel, undoManager)
    return true
}

internal fun KeyEvent.handleClipboardPaste(
    selectionModel: SelectionModel, textBuffer: TextBuffer, caretModel: CaretModel, undoManager: UndoManager
): Boolean {
    if (!isClipboardShortcut()) return false

    return try {
        val data = systemClipboard.getData(DataFlavor.stringFlavor) as? String ?: return false

        if (selectionModel.hasSelection) {
            selectionModel.deleteSelectedText(textBuffer, caretModel, undoManager)
        }

        val position = caretModel.getCurrentPosition()
        undoManager.addEdit(TextAction.Insert(position.offset, data, position.offset))

        var insertOffset = position.offset
        data.forEach { char ->
            textBuffer.insertChar(char, insertOffset)
            insertOffset++
        }

        caretModel.moveTo(insertOffset)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

private fun KeyEvent.isClipboardShortcut(): Boolean = isControlDown || isMetaDown