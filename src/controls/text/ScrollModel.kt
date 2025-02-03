package controls.text

import kotlin.math.roundToInt

internal class ScrollModel {
    var scrollY = 0
    var scrollX = 0
    var maxScrollY = 0
    var maxScrollX = 0
    var isVerticalHovered = false
    var isHorizontalHovered = false
    var isVerticalDragging = false
    var isHorizontalDragging = false
    private var lastDragY = 0
    private var lastDragX = 0
    private var dragStartScrollY = 0
    private var dragStartScrollX = 0

    fun startVerticalDragging(y: Int) {
        isVerticalDragging = true
        lastDragY = y
        dragStartScrollY = scrollY
    }

    fun startHorizontalDragging(x: Int) {
        isHorizontalDragging = true
        lastDragX = x
        dragStartScrollX = scrollX
    }

    fun stopDragging() {
        isVerticalDragging = false
        isHorizontalDragging = false
    }

    fun updateVerticalDragPosition(y: Int, componentHeight: Int, contentHeight: Int) {
        if (!isVerticalDragging) return

        val deltaY = y - lastDragY
        val scrollRatio = componentHeight.toFloat() / contentHeight
        scrollY = (dragStartScrollY + deltaY / scrollRatio).roundToInt().coerceIn(0, maxScrollY)
    }

    fun updateHorizontalDragPosition(x: Int, componentWidth: Int, contentWidth: Int) {
        if (!isHorizontalDragging) return

        val deltaX = x - lastDragX
        val scrollRatio = componentWidth.toFloat() / contentWidth
        scrollX = (dragStartScrollX + deltaX / scrollRatio).roundToInt().coerceIn(0, maxScrollX)
    }
}