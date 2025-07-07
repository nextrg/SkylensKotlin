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
import org.nextrg.skylens.renderables.Pipelines.ROUND_GRADIENT


object Renderables {
    private fun pie(
        graphics: DrawContext,
        x: Float,
        y: Float,
        progress: Float,
        radius: Float,
        startColor: Int,
        endColor: Int,
        startAngle: Float,
        time: Float,
        invert: Boolean,
        reverse: Boolean
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

        PipelineRenderer.draw(Pipelines.CIRCLE_CHART, buffer.end()) { pass: RenderPass ->
            pass.setUniform("modelViewMat", RenderSystem.getModelViewMatrix())
            pass.setUniform("projMat", RenderSystem.getProjectionMatrix())
            pass.setUniform("startColor", *colorToVec4f(startColor))
            pass.setUniform("endColor", *colorToVec4f(endColor))
            pass.setUniform("center", scaledX, flippedY)
            pass.setUniform("radius", scaledRadius)
            pass.setUniform("progress", progress)
            pass.setUniform("time", time)
            pass.setUniform("startAngle", startAngle)
            pass.setUniform("reverse", if (reverse) 1 else 0)
            pass.setUniform("invert", if (invert) 1 else 0)
        }
    }

    fun drawPie(
        graphics: DrawContext,
        x: Float,
        y: Float,
        progress: Float,
        radius: Float,
        color: Int,
        startAngle: Float,
        time: Float,
        invert: Boolean,
        reverse: Boolean
    ) {
        pie(graphics, x, y, progress, radius, color, color, startAngle, time, invert, reverse)
    }

    fun drawPieGradient(
        graphics: DrawContext,
        x: Float,
        y: Float,
        progress: Float,
        radius: Float,
        startColor: Int,
        endColor: Int,
        startAngle: Float,
        time: Float,
        invert: Boolean,
        reverse: Boolean
    ) {
        pie(graphics, x, y, progress, radius, startColor, endColor, startAngle, time, invert, reverse)
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
        borderWidth: Int
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
            pass.setUniform("borderWidth", borderWidth.toFloat())
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
        startColor: Int,
        endColor: Int,
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

        val flippedYWithHeight = window.framebufferHeight - (scaledY + scaledHeight)
        val flippedY = window.framebufferHeight - scaledY

        val matrix = graphics.matrices.peek().positionMatrix
        val buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)

        buffer.vertex(matrix, scaledX, flippedYWithHeight, 0.0f)
        buffer.vertex(matrix, scaledX, flippedY, 0.0f)
        buffer.vertex(matrix, scaledX + scaledWidth, flippedY, 0.0f)
        buffer.vertex(matrix, scaledX + scaledWidth, flippedYWithHeight, 0.0f)

        PipelineRenderer.draw(
            ROUND_GRADIENT, buffer.end()
        ) { pass: RenderPass ->
            pass.setUniform("modelViewMat", RenderSystem.getModelViewMatrix())
            pass.setUniform("projMat", RenderSystem.getProjectionMatrix())
            pass.setUniform("startColor", *colorToVec4f(startColor))
            pass.setUniform("endColor", *colorToVec4f(endColor))
            pass.setUniform("gradientDirection", gradientDirection)
            pass.setUniform("borderRadius", borderRadius, borderRadius, borderRadius, borderRadius)
            pass.setUniform("borderWidth", borderWidth)
            pass.setUniform("scaleFactor", scale)
            pass.setUniform("size", scaledWidth - borderWidth * 2f, scaledHeight - borderWidth * 2f)
            pass.setUniform("center", scaledX + scaledWidth / 2f, flippedYWithHeight + scaledHeight / 2f)
            pass.setUniform("borderColor", *colorToVec4f(borderColor))
            pass.setUniform("time", time)
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