package org.nextrg.skylens.renderables

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.platform.LogicOp
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gl.UniformType
import net.minecraft.client.render.VertexFormats
import org.nextrg.skylens.Skylens

object Pipelines {
    val CIRCLECHART: RenderPipeline = RenderPipelines.register(
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

    val RADIALLINE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(Skylens.id("radial_line"))
            .withVertexShader(Skylens.id("core/basic_transform"))
            .withFragmentShader(Skylens.id("core/radial_line"))
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withColorLogic(LogicOp.NONE)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
            .withUniform("modelViewMat", UniformType.MATRIX4X4)
            .withUniform("projMat", UniformType.MATRIX4X4)
            .withUniform("lineColor", UniformType.VEC4)
            .withUniform("center", UniformType.VEC2)
            .withUniform("radius", UniformType.FLOAT)
            .withUniform("startAngle", UniformType.FLOAT)
            .withUniform("angleThickness", UniformType.FLOAT)
            .withUniform("fadeSoftness", UniformType.FLOAT)
            .withUniform("thickness", UniformType.FLOAT)
            .withUniform("mode", UniformType.INT)
            .build()
    )
}