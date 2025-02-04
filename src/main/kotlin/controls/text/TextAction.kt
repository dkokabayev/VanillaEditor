package controls.text

internal sealed class TextAction {
    internal data class Insert(
        val position: Int,
        val text: String,
        val caretPosition: Int
    ) : TextAction()

    internal data class Delete(
        val position: Int,
        val text: String,
        val caretPosition: Int
    ) : TextAction()
}