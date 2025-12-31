package org.nextrg.skylens.helpers

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState
import org.joml.Vector2f
import org.joml.Vector4f
import kotlin.math.abs
import kotlin.math.pow

object VariablesUtil {
    private const val DEG_TO_RAD = (Math.PI / 180.0).toFloat()
    private const val RAD_TO_DEG = (180.0 / Math.PI).toFloat()

    fun degreesToRadians(degrees: Float): Float = degrees * DEG_TO_RAD
    fun radiansToDegrees(radians: Float): Float = radians * RAD_TO_DEG

    fun withAlpha(hex: Int, alpha: Int = 255): Int = (alpha shl 24) or (hex and 0x00FFFFFF)
    fun sToMs(seconds: Float): Long = (seconds * 1000).toLong()
    fun getAlphaProgress(amount: Float): Int = (255 * amount).toInt()

    fun linear(t: Float) = t
    fun quad(t: Float): Float {
        return if (t <= 0.5f) {
            2.0f * t * t
        } else {
            val shifted = t - 0.5f
            2.0f * shifted * (1.0f - shifted) + 0.5f
        }
    }

    fun animateFloat(
        start: Float,
        end: Float,
        durationMs: Long,
        easing: (Float) -> Float = ::linear
    ): Flow<Float> = flow {
        val startTime = System.currentTimeMillis()
        var fraction: Float
        do {
            val elapsed = System.currentTimeMillis() - startTime
            fraction = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)

            val easedFraction = easing(fraction)

            val value = start + (end - start) * easedFraction
            emit(value)
            delay(10)
        } while (fraction < 1f)
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

    /**
     * Returns a list of colors forming a rainbow based on amount of steps
     * @param steps affects how detailed the gradient is
     * @param brightness value in hsv() of the color
     * @see hsvToRgb
     */
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

    /**
     * Returns the (scaled) center from an element with padding allowing for subpixel movement, requires float x0/x1 and y0/y1
     */
    @JvmStatic
    fun getFloatCenter(state: PictureInPictureRenderState, fx: Vector2f, fy: Vector2f, scale: Float): Vector2f {
        val x0 = state.x0(); val x1 = state.x1()
        val y0 = state.y0(); val y1 = state.y1()
        val fx0 = fx.x; val fx1 = fx.y
        val fy0 = fy.x; val fy1 = fy.y

        val geometricCenterX = ((x1 - x0) / 2f) - 2f
        val geometricCenterY = ((y1 - y0) / 2f) + 2f

        val floatCenterX: Float = ((x0 + x1) - (fx0 + fx1)) * -0.5f
        val floatCenterY: Float = ((y0 + y1) - (fy0 + fy1)) * 0.5f

        return Vector2f(
            (geometricCenterX + floatCenterX) * scale,
            (geometricCenterY + floatCenterY) * scale
        )
    }

    fun listToVector4fArray(colors: List<Int>): Array<Vector4f> {
        return colors.map { color ->
            intToVector4f(color)
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

    fun Int.toARGB(): IntArray {
        return intArrayOf(
            (this ushr 24) and 0xFF, // A
            (this ushr 16) and 0xFF, // R
            (this ushr 8) and 0xFF, // G
            this and 0xFF // B
        )
    }

    fun Float.toFixed(decimals: Int): Float {
        val factor = 10.0.pow(decimals).toFloat()
        return kotlin.math.round(this * factor) / factor
    }
}
