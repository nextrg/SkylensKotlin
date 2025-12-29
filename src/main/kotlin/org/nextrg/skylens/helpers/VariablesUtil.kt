package org.nextrg.skylens.helpers

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.joml.Vector4f
import kotlin.math.abs
import kotlin.math.pow

object VariablesUtil {
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

    fun getGradient(
        startColor: Int,
        endColor: Int,
        steps: Int = 8,
        phase: Float
    ): List<Int> {
        val (sA, sR, sG, sB) = startColor.toARGB()
        val (eA, eR, eG, eB) = endColor.toARGB()

        return List(steps) { i ->
            val t = ((i.toFloat() / (steps - 1)) + phase) % 1f
            val tFinal = if (t <= 0.5f) t * 2f else (1f - t) * 2f

            val a = (sA + ((eA - sA) * tFinal)).toInt()
            val r = (sR + ((eR - sR) * tFinal)).toInt()
            val g = (sG + ((eG - sG) * tFinal)).toInt()
            val b = (sB + ((eB - sB) * tFinal)).toInt()
            (a shl 24) or (r shl 16) or (g shl 8) or b
        }
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

    fun getAlphaProgress(amount: Float): Int {
        return (255 * amount).toInt()
    }

    fun degreesToRadians(degrees: Float): Float {
        return degrees * (Math.PI.toFloat() / 180f)
    }

    fun radiansToDegrees(radians: Float): Float {
        return radians * (180f / Math.PI.toFloat())
    }

    fun hexStringToInt(colorString: String, defaultColor: Int = 0xFF000000.toInt()): Int {
        val color = colorString.trim().removePrefix("#")
        return try {
            when (color.length) {
                6 -> {
                    val rgb = color.toLong(16).toInt()
                    (0xFF shl 24) or (rgb and 0xFFFFFF)
                }
                8 -> {
                    color.toLong(16).toInt()
                }
                else -> defaultColor
            }
        } catch (e: Exception) {
            defaultColor
        }
    }

    fun getRainbow(steps: Int, brightness: Float = 1f): MutableList<Int> {
        val colors = mutableListOf<Int>()
        val hueSt = 360f / steps

        for (i in 0 until steps) {
            val hue = (hueSt * i) % 360f
            colors.add(hsvToRgb(hue, value = brightness.coerceIn(0f, 1f)))
        }

        return colors
    }

    fun hsvToRgb(hue: Float, saturation: Float = 1f, value: Float = 1f): Int {
        val c = value * saturation
        val x = c * (1 - abs((hue / 60f) % 2 - 1))
        val m = value - c

        val (r1, g1, b1) = when {
            hue < 60f -> Triple(c, x, 0f)
            hue < 120f -> Triple(x, c, 0f)
            hue < 180f -> Triple(0f, c, x)
            hue < 240f -> Triple(0f, x, c)
            hue < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        val r = ((r1 + m) * 255).toInt()
        val g = ((g1 + m) * 255).toInt()
        val b = ((b1 + m) * 255).toInt()
        val a = 255

        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun listToVector4fArray(colors: List<Int>): Array<Vector4f> {
        return colors.map { color ->
            Vector4f(
                ((color shr 16) and 0xFF) / 255f,
                ((color shr 8) and 0xFF) / 255f,
                (color and 0xFF) / 255f,
                ((color shr 24) and 0xFF) / 255f
            )
        }.toTypedArray()
    }

    fun intToVector4f(color: Int): Vector4f {
        return Vector4f(
            ((color shr 16) and 0xFF) / 255f,
            ((color shr 8) and 0xFF) / 255f,
            (color and 0xFF) / 255f,
            ((color shr 24) and 0xFF) / 255f
        )
    }

    private fun Int.toARGB(): IntArray {
        return intArrayOf(
            (this ushr 24) and 0xFF, // A
            (this ushr 16) and 0xFF, // R
            (this ushr 8) and 0xFF, // G
            this and 0xFF // B
        )
    }
}