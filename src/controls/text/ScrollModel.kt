package controls.text

internal class ScrollModel {
    var scrollY = 0
    var maxScrollY = 0
    var isHovered = false
    var isDragging = false
    private var lastDragY = 0
    private var dragStartScrollY = 0

    fun startDragging(y: Int) {
        isDragging = true
        lastDragY = y
        dragStartScrollY = scrollY
    }

    fun stopDragging() {
        isDragging = false
    }

    fun updateDragPosition(y: Int, componentHeight: Int, contentHeight: Int) {
        if (isDragging) {
            val deltaY = y - lastDragY
            val scrollRatio = componentHeight.toFloat() / contentHeight
            scrollY = (dragStartScrollY + deltaY / scrollRatio).toInt().coerceIn(0, maxScrollY)
        }
    }
}