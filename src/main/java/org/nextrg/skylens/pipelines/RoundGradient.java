package org.nextrg.skylens.pipelines;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import earth.terrarium.olympus.client.utils.GuiGraphicsHelper;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexFormats;
import org.joml.Vector4f;
import org.nextrg.skylens.Skylens;
import org.nextrg.skylens.pipelines.pips.RoundGradientPIPRenderer;
import org.nextrg.skylens.pipelines.uniforms.RoundGradientUniform;

public class RoundGradient {
    public static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Skylens.Companion.id("core/round_gradient"))
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("RoundGradientUniform", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withFragmentShader(Skylens.Companion.id("core/round_gradient"))
            .withVertexShader(Skylens.Companion.id("core/basic_transform"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .build();
    
    /**
     * Draws a 2D gradient rectangle with rounded corners on UI using Olympus rounded rectangle, but with float variables.
     * @param drawContext Context used to draw the ui element
     * @param x X position of <b>left</b> corner of the rectangle
     * @param y Y position of <b>top</b> corner of the rectangle
     *
     * @see RoundGradientUniform
     * @see RoundGradientPIPRenderer
     */
    public static void draw(DrawContext drawContext, float x, float y, float width, float height, Vector4f[] colors, float time, int gradientDirection, int borderColor, float borderRadius, float borderWidth) {
        RoundGradientPIPRenderer.State state = new RoundGradientPIPRenderer.State(
                drawContext,
                x + 2f, y + 2f,
                width, height,
                0xFFFFFFFF,
                colors, colors.length, time,
                gradientDirection,
                borderColor, borderRadius, borderWidth
        );
        
        GuiGraphicsHelper.submitPip(drawContext, state);
    }
}
