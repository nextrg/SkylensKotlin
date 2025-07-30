package org.nextrg.skylens.helpers

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.item.ItemStack
import java.lang.Math.clamp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object RenderUtil {
    data class ElementPos(
        val anchorKey: String, val offsetX: Float, val offsetY: Float,
        val isBar: Boolean = false, val clampX: (Float, Int) -> Float, val clampY: (Float, Int) -> Float
    )

    val anchors: Map<String, FloatArray> = java.util.Map.of(
        "TopLeft", floatArrayOf(0f, 0f), // (X, Y)
        "MiddleLeft", floatArrayOf(0f, 0.5f),
        "BottomLeft", floatArrayOf(0f, 1f),
        "TopRight", floatArrayOf(1f, 0f),
        "MiddleRight", floatArrayOf(1f, 0.5f),
        "BottomRight", floatArrayOf(1f, 1f),
        "TopMiddle", floatArrayOf(0.5f, 0f),
        "BottomMiddle", floatArrayOf(0.5f, 1f)
    )

    private val cachedPositions = mutableMapOf<String, Pair<Float, Float>>()
    private val lastStates = mutableMapOf<String, String>()

    fun computePosition(config: ElementPos): Pair<Float, Float> {
        val client = MinecraftClient.getInstance()
        val screenW = client.window.scaledWidth
        val screenH = client.window.scaledHeight

        val configState = "${config.anchorKey}|$screenW|$screenH|${config.offsetX}|${config.offsetY}|${config.isBar}"
        val last = lastStates[config.anchorKey]

        if (configState != last) {
            val (ax, ay) = anchors[config.anchorKey] ?: floatArrayOf(0.5f, 1f)
            val anchorX = screenW * ax - 50 * ax
            val anchorY = screenH * ay - 8 * ay
            val marginX = 2 * (1 - ax * 2)
            val marginY = 2 * (1 - ay * 2)

            val x = config.clampX(anchorX + marginX + config.offsetX, screenW)
            val y = config.clampY(anchorY + marginY + config.offsetY, screenH)

            cachedPositions[config.anchorKey] = x to y
            lastStates[config.anchorKey] = configState
        }

        return cachedPositions[config.anchorKey]!!
    }

    fun getScaledWidthHeight(): Pair<Int, Int> {
        val client = MinecraftClient.getInstance()
        return client.window.scaledWidth to client.window.scaledHeight
    }

    fun legacyRoundRectangle(context: DrawContext, x: Float, y: Float, w: Float, h: Float, r: Float, color: Int) {
        val radius = clamp(r, 1f, min(w, h) / 2)
        val corners = arrayOf(
            floatArrayOf(x + w - radius, y + radius),
            floatArrayOf(x + w - radius, y + h - radius),
            floatArrayOf(x + radius, y + h - radius),
            floatArrayOf(x + radius, y + radius)
        )
        context.draw { source: VertexConsumerProvider ->
            val matrix4f = context.matrices.peek().positionMatrix
            val buffer = source.getBuffer(RenderLayer.getDebugTriangleFan())
            buffer.vertex(matrix4f, x + w / 2f, y + h / 2f, 0f).color(color)
            for (corner in 0..3) {
                val cornerStart = (corner - 1) * 90
                val cornerEnd = cornerStart + 90
                var i = cornerStart
                while (i <= cornerEnd) {
                    val angle = Math.toRadians(i.toDouble()).toFloat()
                    val rx = corners[corner][0] + (cos(angle.toDouble()) * radius).toFloat()
                    val ry = corners[corner][1] + (sin(angle.toDouble()) * radius).toFloat()
                    buffer.vertex(matrix4f, rx, ry, 0f).color(color)
                    i += 10
                }
            }
            buffer.vertex(matrix4f, corners[0][0], y, 0f).color(color)
        }
    }

    fun drawItem(context: DrawContext, item: ItemStack?, x: Float, y: Float, scale: Float) {
        if (item == null || item.isEmpty) return

        context.matrices.push()
        context.matrices.translate(x, y, 0f)

        val offset = 8f * (scale - 1f)
        context.matrices.translate(-offset, -offset, 0f)
        context.matrices.scale(scale, scale, 1f)

        context.drawItem(item, 0, 0)

        context.matrices.pop()
    }

    fun drawText(context: DrawContext, text: String?, x: Float, y: Float, color: Int, scale: Float, centered: Boolean, shadow: Boolean) {
        val textRenderer = MinecraftClient.getInstance().textRenderer
        val width = textRenderer.getWidth(text) * scale
        val transformX = if (centered) x - width / 2f else x

        context.matrices.push()
        context.matrices.translate(transformX, y, 1f)
        context.matrices.scale(scale, scale, scale)

        context.drawText(textRenderer, text, 0, 0, color, shadow)

        context.matrices.pop()
    }
}