package org.nextrg.skylens.renderables

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
import org.nextrg.skylens.renderables.Rendering.colorToVec4f

object CircleChart {
    private val PIPELINE: RenderPipeline = RenderPipelines.register(
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
            .withUniform("invert", UniformType.INT)
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

        PipelineRenderer.draw(PIPELINE, buffer.end()) { pass: RenderPass ->
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
}