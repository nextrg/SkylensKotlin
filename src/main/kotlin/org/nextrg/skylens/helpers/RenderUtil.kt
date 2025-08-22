package org.nextrg.skylens.helpers

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.ScreenRect
import net.minecraft.item.ItemStack
import kotlin.math.roundToInt

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

    fun floatToIntScreenRect(x: Float, y: Float, w: Float, h: Float): ScreenRect {
        return ScreenRect(x.roundToInt(), y.roundToInt(), w.roundToInt(), h.roundToInt())
    }

    fun legacyRoundRectangle(context: DrawContext, x: Float, y: Float, w: Float, h: Float, r: Float, color: Int) {
        // Replace this with the float round rect shader
    }

    fun drawItem(context: DrawContext, item: ItemStack?, x: Float, y: Float, scale: Float) {
        if (item == null || item.isEmpty) return

        context.matrices.pushMatrix()
        context.matrices.translate(x, y)

        val offset = 8f * (scale - 1f)
        context.matrices.translate(-offset, -offset)
        context.matrices.scale(scale, scale)

        context.drawItem(item, 0, 0)

        context.matrices.popMatrix()
    }

    fun drawText(context: DrawContext, text: String?, x: Float, y: Float, color: Int, scale: Float, centered: Boolean, shadow: Boolean) {
        val textRenderer = MinecraftClient.getInstance().textRenderer
        val width = textRenderer.getWidth(text) * scale
        val transformX = if (centered) x - width / 2f else x

        context.matrices.pushMatrix()
        context.matrices.translate(transformX, y)
        context.matrices.scale(scale, scale)

        context.drawText(textRenderer, text, 0, 0, color, shadow)

        context.matrices.popMatrix()
    }
}
