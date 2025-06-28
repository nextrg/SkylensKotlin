package org.nextrg.skylens.renderables

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.item.ItemStack
import java.lang.Math.clamp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object Rendering {
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
        context.matrices.translate(transformX, y, 0f)
        context.matrices.scale(scale, scale, scale)

        context.drawText(textRenderer, text, 0, 0, color, shadow)

        context.matrices.pop()
    }

    fun colorToVec4f(color: Int): FloatArray {
        return floatArrayOf(
            (color shr 16 and 0xFF) / 255f,
            (color shr 8 and 0xFF) / 255f,
            (color and 0xFF) / 255f,
            (color shr 24 and 0xFF) / 255f
        )
    }
}