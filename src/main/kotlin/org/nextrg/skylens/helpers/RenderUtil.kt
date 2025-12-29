package org.nextrg.skylens.helpers

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack

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
        val client = Minecraft.getInstance()
        val screenW = client.window.guiScaledWidth
        val screenH = client.window.guiScaledHeight

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
        val client = Minecraft.getInstance()
        return client.window.guiScaledWidth to client.window.guiScaledHeight
    }

    fun drawItem(guiGraphics: GuiGraphics, item: ItemStack?, x: Float, y: Float, scale: Float) {
        if (item == null || item.isEmpty) return

        guiGraphics.pose().pushMatrix()
        guiGraphics.pose().translate(x, y)

        val offset = 8f * (scale - 1f)
        guiGraphics.pose().translate(-offset, -offset)
        guiGraphics.pose().scale(scale, scale)

        guiGraphics.renderItem(item, 0, 0)

        guiGraphics.pose().popMatrix()
    }

    fun drawText(guiGraphics: GuiGraphics, text: String, x: Float, y: Float, color: Int, scale: Float, centered: Boolean, shadow: Boolean) {
        val font = Minecraft.getInstance().font
        val width = font.width(text) * scale
        val transformX = if (centered) x - width / 2f else x

        guiGraphics.pose().pushMatrix()
        guiGraphics.pose().translate(transformX, y)
        guiGraphics.pose().scale(scale, scale)

        guiGraphics.drawString(font, text, 0, 0, color, shadow)

        guiGraphics.pose().popMatrix()
    }
}
