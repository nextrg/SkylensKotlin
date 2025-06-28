package org.nextrg.skylens.client.rendering

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.platform.LogicOp
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import earth.terrarium.olympus.client.pipelines.PipelineRenderer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gl.UniformType
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import org.nextrg.skylens.Skylens
import org.nextrg.skylens.helpers.Rendering.colorToVec4f

object CircleChart {
    private val PROGRESS_CHART: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(Skylens.id("circle_chart"))
            .withVertexShader(Skylens.id("core/basic_transform"))
            .withFragmentShader(Skylens.id("core/circle_chart"))
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withColorLogic(LogicOp.NONE)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
            .withUniform("modelViewMat", UniformType.MATRIX4X4)
            .withUniform("projMat", UniformType.MATRIX4X4)
            .withUniform("startColor", UniformType.VEC4)
            .withUniform("endColor", UniformType.VEC4)
            .withUniform("center", UniformType.VEC2)
            .withUniform("radius", UniformType.FLOAT)
            .withUniform("progress", UniformType.FLOAT)
            .withUniform("time", UniformType.FLOAT)
            .withUniform("startAngle", UniformType.FLOAT)
            .withUniform("reverse", UniformType.INT)
            .build()
    )

    fun draw(
        graphics: DrawContext,
        x: Int,
        y: Int,
        progress: Float,
        radius: Float,
        startColor: Int,
        endColor: Int,
        startAngle: Float,
        time: Float,
        invert: Boolean,
        borderWidth: Float,
        borderColor: Int
    ) {
        val window = MinecraftClient.getInstance().window
        val scale = window.scaleFactor.toFloat()
        val scaledX = x * scale
        val scaledY = y * scale
        val scaledRadius = radius * scale

        val yOffset = window.framebufferHeight - scaledY * 2.0f
        val matrix = graphics.matrices.peek().positionMatrix
        val buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)

        buffer.vertex(matrix, x - radius, y - radius, 1.0f)
        buffer.vertex(matrix, x - radius, y + radius, 1.0f)
        buffer.vertex(matrix, x + radius, y + radius, 1.0f)
        buffer.vertex(matrix, x + radius, y - radius, 1.0f)

        PipelineRenderer.draw(
            PROGRESS_CHART, buffer.end()
        ) { pass: RenderPass ->
            pass.setUniform("modelViewMat", RenderSystem.getModelViewMatrix())
            pass.setUniform("projMat", RenderSystem.getProjectionMatrix())
            pass.setUniform("startColor", *colorToVec4f(startColor))
            pass.setUniform("endColor", *colorToVec4f(endColor))
            pass.setUniform("center", scaledX, scaledY + yOffset)
            pass.setUniform("radius", scaledRadius)
            pass.setUniform("progress", progress)
            pass.setUniform("time", time)
            pass.setUniform("startAngle", startAngle)
            pass.setUniform("borderWidth", borderWidth)
            pass.setUniform("borderColor", *colorToVec4f(borderColor))
            pass.setUniform("reverse", if (invert) 1 else 0)
        }
    }
}