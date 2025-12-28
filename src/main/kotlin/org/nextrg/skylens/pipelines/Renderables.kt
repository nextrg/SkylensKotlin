package org.nextrg.skylens.pipelines

import net.minecraft.client.gui.DrawContext
import org.joml.Vector2f
import org.nextrg.skylens.helpers.VariablesUtil.intToVector4f
import org.nextrg.skylens.helpers.VariablesUtil.listToVector4fArray

object Renderables {
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
        CircleChart.draw(graphics, x, y, Pair(outerRadius, innerRadius), listToVector4fArray(listOf(color)), progress, time, startAngle, invert, reverse);
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
        CircleChart.draw(graphics, x, y, Pair(outerRadius, innerRadius), listToVector4fArray(color), progress, time, startAngle, invert, reverse);
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
        /*
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
        }*/
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
        RoundRectangleFloat.draw(graphics, x, y, width, height, backgroundColor, borderColor, borderRadius, borderWidth)
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
        /*
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
         */
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
        borderColor: Int, // UNUSED
        borderRadius: Float,
        borderWidth: Float // UNUSED
    ) {
        FluidContainer.draw(graphics, x, y, width, height, intToVector4f(color), waveDirection, Vector2f(offset.first, offset.second), borderRadius);
        /*
        val window = MinecraftClient.getInstance().window
        val scale = window.scaleFactor.toFloat()
        val scaledX = x * scale
        val scaledY = y * scale
        val scaledWidth = width * scale
        val scaledHeight = height * scale
        val yOffset = window.framebufferHeight.toFloat() - scaledHeight - scaledY * 2.0f

        val matrix = graphics.matrices
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
            pass.setUniform("offset", Vector2f(offset.first, offset.second)) // Changed two floats to vector2
            pass.setUniform("waveDirection", waveDirection)
        }
         */
    }
}