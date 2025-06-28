package org.nextrg.skylens.helpers

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.awt.Color
import kotlin.math.pow

object Variables {
    fun animateFloat(
        start: Float,
        end: Float,
        durationMs: Long,
        easing: (Float) -> Float = ::linear
    ): Flow<Float> = flow {
        val startTime = System.currentTimeMillis()
        var fraction: Float
        do {
            val now = System.currentTimeMillis()
            val elapsed = now - startTime
            fraction = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)

            val easedFraction = easing(fraction)

            val value = start + (end - start) * easedFraction
            emit(value)
            delay(10)
        } while (fraction < 1f)
    }

    fun linear(t: Float) = t

    fun quad(t: Float): Float {
        return if (t <= 0.5f) {
            2.0f * t * t
        } else {
            val shifted = t - 0.5f
            2.0f * shifted * (1.0f - shifted) + 0.5f
        }
    }

    fun Float.toFixed(decimals: Int): Float {
        val factor = 10.0.pow(decimals).toFloat()
        return kotlin.math.round(this * factor) / factor
    }

    fun colorToARGB(color: Color): Int {
        return (color.alpha shl 24) or (color.red shl 16) or (color.green shl 8) or color.blue
    }

    fun hexTransparent(hex: Int, alpha: Int): Int {
        return (alpha shl 24) or (hex and 0x00FFFFFF)
    }

    fun hexOpaque(hexa: Int): Int {
        return (0xFF shl 24) or (hexa and 0x00FFFFFF)
    }

    fun sToMs(seconds: Float): Long {
        return (seconds * 1000).toLong()
    }
}