package org.nextrg.skylens.renderables

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gl.UniformType
import net.minecraft.client.render.VertexFormats
import org.nextrg.skylens.Skylens

object Pipelines {
    val CIRCLE_CHART: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(Skylens.id("circle_chart"))
            .withVertexShader(Skylens.id("core/basic_transform"))
            .withFragmentShader(Skylens.id("core/circle_chart"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
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

    val ROUND_GRADIENT: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(Skylens.id("round_gradient"))
            .withVertexShader(Skylens.id("core/basic_transform"))
            .withFragmentShader(Skylens.id("core/round_gradient"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
            .withUniform("modelViewMat", UniformType.MATRIX4X4)
            .withUniform("projMat", UniformType.MATRIX4X4)
            .withUniform("startColor", UniformType.VEC4)
            .withUniform("endColor", UniformType.VEC4)
            .withUniform("size", UniformType.VEC2)
            .withUniform("center", UniformType.VEC2)
            .withUniform("borderColor", UniformType.VEC4)
            .withUniform("borderRadius", UniformType.VEC4)
            .withUniform("borderWidth", UniformType.FLOAT)
            .withUniform("scaleFactor", UniformType.FLOAT)
            .withUniform("time", UniformType.FLOAT)
            .withUniform("gradientDirection", UniformType.INT)
            .build()
    )

    val RADIAL_LINE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(Skylens.id("radial_line"))
            .withVertexShader(Skylens.id("core/basic_transform"))
            .withFragmentShader(Skylens.id("core/radial_line"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
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