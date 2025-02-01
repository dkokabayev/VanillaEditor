package controls.text

internal data class CaretPosition(
    val offset: Int,
    val start: Int,
    val end: Int,
    val text: String
)