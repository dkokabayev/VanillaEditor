package controls.text

internal data class CaretPosition(
    val offset: Int,
    val lineStart: Int,
    val lineEnd: Int,
    val lineText: String
)