package controls.text

import javax.swing.Timer
import kotlin.math.exp

internal class RepeatableAction(
    private val initialDelay: Int,
    private val accelerationFactor: Double,
    private val minDelay: Int,
    private val action: () -> Unit
) {
    private var isPressed = false
    private var repeatTimer: Timer? = null
    private var pressStartTime: Long = 0

    fun start() {
        if (!isPressed) {
            isPressed = true
            pressStartTime = System.currentTimeMillis()

            action()
            startRepeatTimer()
        }
    }

    fun stop() {
        if (isPressed) {
            isPressed = false
            repeatTimer?.stop()
            repeatTimer = null
        }
    }

    private fun calculateCurrentDelay(): Int {
        val elapsedTime = System.currentTimeMillis() - pressStartTime
        val factor = exp(-accelerationFactor * elapsedTime / 1000.0)
        val currentRate = (initialDelay * factor + minDelay).toInt()
        return currentRate.coerceIn(minDelay, initialDelay)
    }

    private fun startRepeatTimer() {
        repeatTimer?.stop()
        repeatTimer = Timer(initialDelay) {
            if (isPressed) {
                action()
                val currentDelay = calculateCurrentDelay()
                repeatTimer?.delay = currentDelay
            } else {
                repeatTimer?.stop()
                repeatTimer = null
            }
        }.apply { start() }
    }
}