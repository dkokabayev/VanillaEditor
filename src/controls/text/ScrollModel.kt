package controls.text

import java.awt.Point

internal class ScrollModel {
    var scrollY = 0
    var maxScrollY = 0
    var isHovered = false
    var isDragging = false
    var lastDragY = 0
    var dragStartScrollY = 0

    fun updateHover(point: Point, width: Int, scrollBarWidth: Int, contentHeight: Int, componentHeight: Int): Boolean {
        val wasHovered = isHovered
        isHovered = isInScrollBar(point, width, scrollBarWidth, contentHeight, componentHeight)
        return wasHovered != isHovered
    }

    private fun isInScrollBar(point: Point, width: Int, scrollBarWidth: Int, contentHeight: Int, componentHeight: Int): Boolean {
        return point.x >= width - scrollBarWidth && contentHeight > componentHeight
    }

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

