package controls.text

import java.awt.FontMetrics
import java.awt.event.KeyEvent

internal fun KeyEvent.handleSelectionForNavigation(
    selectionModel: SelectionModel, caretModel: CaretModel, navigationAction: () -> Unit
) {
    if (!isShiftDown) {
        selectionModel.clearSelection()
    } else if (!selectionModel.hasSelection) {
        selectionModel.startSelection(caretModel.position)
    }

    navigationAction()

    if (isShiftDown) {
        selectionModel.updateSelection(caretModel.position)
    }
}

internal fun KeyEvent.handlePageNavigation(
    selectionModel: SelectionModel,
    caretModel: CaretModel,
    metrics: FontMetrics,
    viewportHeight: Int,
    padding: Int,
    isPageUp: Boolean
) {
    handleSelectionForNavigation(selectionModel, caretModel) {
        val linesPerPage = (viewportHeight - 2 * padding) / metrics.height
        repeat(linesPerPage) {
            if (isPageUp) {
                caretModel.moveUpWithOption()
            } else {
                caretModel.moveDownWithOption()
            }
        }
    }
}

internal fun SelectionModel.deleteSelectedText(
    textBuffer: TextBuffer, caretModel: CaretModel, undoManager: UndoManager? = null
): Boolean {
    val selection = getCurrentSelection() ?: return false

    undoManager?.addEdit(
        TextAction.Delete(
            position = selection.start, text = selection.text, caretPosition = caretModel.getCurrentPosition().offset
        )
    )

    for (i in selection.end - 1 downTo selection.start) {
        textBuffer.deleteCharAt(i)
    }
    caretModel.moveTo(selection.start)
    clearSelection()

    return true
}