package org.nextrg.skylens.renderables

import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import earth.terrarium.olympus.client.pipelines.PipelineRenderer
import earth.terrarium.olympus.client.pipelines.RoundedRectangle
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import org.nextrg.skylens.renderables.Pipelines.FLUID_CONTAINER
import org.nextrg.skylens.renderables.Pipelines.ROUND_GRADIENT

object Renderables {
    private fun pie(
        graphics: DrawContext,
        x: Float,
        y: Float,
        progress: Float,
        outerRadius: Float,
        innerRadius: Float,
        color: List<Int>,
        startAngle: Float,
        time: Float,
        invert: Boolean,
        reverse: Boolean
    ) {
        val window = MinecraftClient.getInstance().window
        val scale = window.scaleFactor.toFloat()
        val scaledX = x * scale
        val scaledY = y * scale
        val scaledOuterRadius = outerRadius * scale
        val scaledInnerRadius = innerRadius * scale

        val flippedY = window.framebufferHeight - scaledY

        val matrix = graphics.matrices.peek().positionMatrix
        val buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)

        buffer.vertex(matrix, x - outerRadius, y - outerRadius, 0.0f)
        buffer.vertex(matrix, x - outerRadius, y + outerRadius, 0.0f)
        buffer.vertex(matrix, x + outerRadius, y + outerRadius, 0.0f)
        buffer.vertex(matrix, x + outerRadius, y - outerRadius, 0.0f)

        val maxColors = 8

        PipelineRenderer.draw(Pipelines.CIRCLE_CHART, buffer.end()) { pass: RenderPass ->
            pass.setUniform("modelViewMat", RenderSystem.getModelViewMatrix())
            pass.setUniform("projMat", RenderSystem.getProjectionMatrix())
            val baseColors = color.ifEmpty { listOf(0xFFFFFFFF.toInt()) }
            val safeColors = if (baseColors.size >= maxColors) {
                baseColors.subList(0, maxColors)
            } else {
                baseColors + List(maxColors - baseColors.size) { baseColors.last() }
            }
            for (i in 0 until maxColors) {
                pass.setUniform("color$i", *colorToVec4f(safeColors[i]))
            }
            val colorCountSafe = baseColors.size.coerceIn(1, maxColors)
            pass.setUniform("colorCount", colorCountSafe)
            pass.setUniform("center", scaledX, flippedY)
            pass.setUniform("outerRadius", scaledOuterRadius)
            pass.setUniform("innerRadius", scaledInnerRadius)
            pass.setUniform("progress", progress)
            pass.setUniform("time", time)
            pass.setUniform("startAngle", startAngle + Math.PI.toFloat() / 2)
            pass.setUniform("reverse", if (reverse) 1 else 0)
            pass.setUniform("invert", if (invert) 1 else 0)
        }
    }

    fun drawPie(
        graphics: DrawContext,
        x: Float,
        y: Float,
        progress: Float,
        outerRadius: Float,
        innerRadius: Float,
        color: Int,
        startAngle: Float,
        time: Float,
        invert: Boolean = false,
        reverse: Boolean = false
    ) {
        pie(graphics, x, y, progress, outerRadius, innerRadius, mutableListOf(color), startAngle, time, invert, reverse)
    }

    fun drawPieGradient(
        graphics: DrawContext,
        x: Float,
        y: Float,
        progress: Float,
        outerRadius: Float,
        innerRadius: Float,
        color: List<Int>,
        startAngle: Float,
        time: Float,
        invert: Boolean = false,
        reverse: Boolean = false
    ) {
        pie(graphics, x, y, progress, outerRadius, innerRadius, color, startAngle, time, invert, reverse)
    }

    fun drawLine(
        graphics: DrawContext,
        x: Float,
        y: Float,
        progress: Float,
        radius: Float,
        lineColor: Int,
        angleThickness: Float,
        lineThickness: Float,
        fadeSoftness: Float,
        mode: Int
    ) {
        val window = MinecraftClient.getInstance().window
        val scale = window.scaleFactor.toFloat()
        val scaledX = x * scale
        val scaledY = y * scale
        val scaledRadius = radius * scale

        val flippedY = window.framebufferHeight - scaledY

        val matrix = graphics.matrices.peek().positionMatrix
        val buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)

        buffer.vertex(matrix, x - radius, y - radius, 0.0f)
        buffer.vertex(matrix, x - radius, y + radius, 0.0f)
        buffer.vertex(matrix, x + radius, y + radius, 0.0f)
        buffer.vertex(matrix, x + radius, y - radius, 0.0f)

        PipelineRenderer.draw(Pipelines.RADIAL_LINE, buffer.end()) { pass: RenderPass ->
            pass.setUniform("modelViewMat", RenderSystem.getModelViewMatrix())
            pass.setUniform("projMat", RenderSystem.getProjectionMatrix())
            pass.setUniform("lineColor", *colorToVec4f(lineColor))
            pass.setUniform("center", scaledX, flippedY)
            pass.setUniform("radius", scaledRadius)
            pass.setUniform("startAngle", progress)
            pass.setUniform("angleThickness", angleThickness)
            pass.setUniform("fadeSoftness", fadeSoftness)
            pass.setUniform("thickness", lineThickness)
            pass.setUniform("mode", mode)
        }
    }

    fun roundRectangleFloat(
        graphics: DrawContext,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        backgroundColor: Int,
        borderColor: Int,
        borderRadius: Float,
        borderWidth: Float
    ) {
        val window = MinecraftClient.getInstance().window
        val scale = window.scaleFactor.toFloat()
        val scaledX = x * scale
        val scaledY = y * scale
        val scaledWidth = width * scale
        val scaledHeight = height * scale

        val flippedYWithHeight = window.framebufferHeight - (scaledY + scaledHeight)

        val matrix = graphics.matrices.peek().positionMatrix
        val buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        buffer.vertex(matrix, x, y, 0.0f).color(backgroundColor)
        buffer.vertex(matrix, x, (y + height), 0.0f).color(backgroundColor)
        buffer.vertex(matrix, (x + width), (y + height), 0.0f).color(backgroundColor)
        buffer.vertex(matrix, (x + width), y, 0.0f).color(backgroundColor)

        PipelineRenderer.draw(RoundedRectangle.PIPELINE, buffer.end()) { pass: RenderPass ->
            pass.setUniform("borderColor", *colorToVec4f(borderColor))
            pass.setUniform("borderRadius", *floatArrayOf(borderRadius, borderRadius, borderRadius, borderRadius))
            pass.setUniform("borderWidth", borderWidth)
            pass.setUniform("size", scaledWidth - borderWidth * 2f, scaledHeight - borderWidth * 2f)
            pass.setUniform("center", scaledX + scaledWidth / 2f, flippedYWithHeight + (scaledHeight / 2f))
            pass.setUniform("scaleFactor", scale)
        }
    }

    fun roundGradient(
        graphics: DrawContext,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: MutableList<Int>,
        gradientDirection: Int,
        time: Float,
        borderColor: Int,
        borderRadius: Float,
        borderWidth: Float
    ) {
        val window = MinecraftClient.getInstance().window
        val scale = window.scaleFactor.toFloat()
        val scaledX = x * scale
        val scaledY = y * scale
        val scaledWidth = width * scale
        val scaledHeight = height * scale
        val yOffset = window.framebufferHeight.toFloat() - scaledHeight - scaledY * 2.0f

        val matrix = graphics.matrices.peek().positionMatrix
        val buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)

        buffer.vertex(matrix, x, y, 0.0f)
        buffer.vertex(matrix, x, (y + height), 0.0f)
        buffer.vertex(matrix, (x + width), (y + height), 0.0f)
        buffer.vertex(matrix, (x + width), y, 0.0f)

        val maxColors = 8

        PipelineRenderer.draw(ROUND_GRADIENT, buffer.end()) { pass: RenderPass ->
            pass.setUniform("modelViewMat", RenderSystem.getModelViewMatrix())
            pass.setUniform("projMat", RenderSystem.getProjectionMatrix())
            val baseColors = color.ifEmpty { listOf(0xFFFFFFFF.toInt()) }
            val safeColors = if (baseColors.size >= maxColors) {
                baseColors.subList(0, maxColors)
            } else {
                baseColors + List(maxColors - baseColors.size) { baseColors.last() }
            }
            for (i in 0 until maxColors) {
                pass.setUniform("color$i", *colorToVec4f(safeColors[i]))
            }
            pass.setUniform("gradientDirection", gradientDirection)
            pass.setUniform("borderRadius", *floatArrayOf(borderRadius, borderRadius, borderRadius, borderRadius))
            pass.setUniform("borderWidth", borderWidth)
            pass.setUniform("scaleFactor", scale)
            pass.setUniform("size", scaledWidth - borderWidth * 2.0f * scale, scaledHeight - borderWidth * 2.0f * scale)
            pass.setUniform("center", scaledX + scaledWidth / 2.0f, scaledY + scaledHeight / 2.0f + yOffset)
            pass.setUniform("borderColor", *colorToVec4f(borderColor))
            pass.setUniform("time", time)
        }
    }

    fun roundFluidContainer(
        graphics: DrawContext,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Int,
        waveDirection: Int,
        offset: Pair<Float, Float>,
        borderColor: Int,
        borderRadius: Float,
        borderWidth: Float
    ) {
        val window = MinecraftClient.getInstance().window
        val scale = window.scaleFactor.toFloat()
        val scaledX = x * scale
        val scaledY = y * scale
        val scaledWidth = width * scale
        val scaledHeight = height * scale
        val yOffset = window.framebufferHeight.toFloat() - scaledHeight - scaledY * 2.0f

        val matrix = graphics.matrices.peek().positionMatrix
        val buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)

        buffer.vertex(matrix, x, y, 0.0f)
        buffer.vertex(matrix, x, (y + height), 0.0f)
        buffer.vertex(matrix, (x + width), (y + height), 0.0f)
        buffer.vertex(matrix, (x + width), y, 0.0f)

        PipelineRenderer.draw(FLUID_CONTAINER, buffer.end()) { pass: RenderPass ->
            pass.setUniform("modelViewMat", RenderSystem.getModelViewMatrix())
            pass.setUniform("projMat", RenderSystem.getProjectionMatrix())
            pass.setUniform("fillColor", *colorToVec4f(color))
            pass.setUniform("borderRadius", *floatArrayOf(borderRadius, borderRadius, borderRadius, borderRadius))
            pass.setUniform("borderWidth", borderWidth)
            pass.setUniform("scaleFactor", scale)
            pass.setUniform("size", scaledWidth - borderWidth * 2.0f * scale, scaledHeight - borderWidth * 2.0f * scale)
            pass.setUniform("center", scaledX + scaledWidth / 2.0f, scaledY + scaledHeight / 2.0f + yOffset)
            pass.setUniform("borderColor", *colorToVec4f(borderColor))
            pass.setUniform("offsetX", offset.first)
            pass.setUniform("offsetY", offset.second)
            pass.setUniform("waveDirection", waveDirection)
        }
    }

    private fun colorToVec4f(color: Int): FloatArray {
        return floatArrayOf(
            (color shr 16 and 0xFF) / 255f,
            (color shr 8 and 0xFF) / 255f,
            (color and 0xFF) / 255f,
            (color shr 24 and 0xFF) / 255f
        )
    }
}