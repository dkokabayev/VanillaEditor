package controls.text

internal class UndoManager {
    private val undoStack = mutableListOf<TextAction>()
    private val redoStack = mutableListOf<TextAction>()
    private var isUndoInProgress = false

    fun addEdit(edit: TextAction) {
        if (!isUndoInProgress) {
            undoStack.add(edit)
            redoStack.clear()
        }
    }

    fun undo(textBuffer: TextBuffer, caretModel: CaretModel): Boolean {
        if (undoStack.isEmpty()) return false

        isUndoInProgress = true
        try {
            val edit = undoStack.removeAt(undoStack.lastIndex)
            when (edit) {
                is TextAction.Insert -> {
                    for (i in edit.text.length - 1 downTo 0) {
                        textBuffer.deleteCharAt(edit.position)
                    }
                    caretModel.moveTo(edit.position)
                    redoStack.add(edit)
                }

                is TextAction.Delete -> {
                    for (i in 0 until edit.text.length) {
                        textBuffer.insertChar(edit.text[i], edit.position + i)
                    }
                    caretModel.moveTo(edit.caretPosition)
                    redoStack.add(edit)
                }
            }
        } finally {
            isUndoInProgress = false
        }
        return true
    }

    fun redo(textBuffer: TextBuffer, caretModel: CaretModel): Boolean {
        if (redoStack.isEmpty()) return false

        isUndoInProgress = true
        try {
            val edit = redoStack.removeAt(redoStack.lastIndex)
            when (edit) {
                is TextAction.Insert -> {
                    for (i in 0 until edit.text.length) {
                        textBuffer.insertChar(edit.text[i], edit.position + i)
                    }
                    caretModel.moveTo(edit.position + edit.text.length)
                    undoStack.add(edit)
                }

                is TextAction.Delete -> {
                    for (i in edit.text.length - 1 downTo 0) {
                        textBuffer.deleteCharAt(edit.position)
                    }
                    caretModel.moveTo(edit.position)
                    undoStack.add(edit)
                }
            }
        } finally {
            isUndoInProgress = false
        }
        return true
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}