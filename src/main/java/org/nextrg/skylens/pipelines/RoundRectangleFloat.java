package org.nextrg.skylens.pipelines;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import earth.terrarium.olympus.client.pipelines.RoundedRectangle;
import earth.terrarium.olympus.client.pipelines.uniforms.RoundedRectangleUniform;
import earth.terrarium.olympus.client.utils.GuiGraphicsHelper;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexFormats;
import org.nextrg.skylens.Skylens;
import org.nextrg.skylens.pipelines.pips.RoundRectangleFloatPIPRenderer;

public class RoundRectangleFloat {
    public static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Skylens.Companion.id("core/round_rect"))
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("RoundedRectangleUniform", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withFragmentShader(Skylens.Companion.id("core/round_rect"))
            .withVertexShader(Skylens.Companion.id("core/basic_transform"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .build();
    
    /**
     * Draws a 2D rectangle with rounded corners on UI using Olympus rounded rectangle, but with float variables.
     * @param drawContext Context used to draw the ui element
     * @param x X position of <b>left</b> corner of the rectangle
     * @param y Y position of <b>top</b> corner of the rectangle
     *
     * @see RoundedRectangle RoundedRectangle</code> (original)
     * @see RoundedRectangleUniform
     * @see RoundRectangleFloatPIPRenderer
     */
    public static void draw(DrawContext drawContext, float x, float y, float width, float height, int backgroundColor, int borderColor, float borderRadius, float borderWidth) {
        RoundRectangleFloatPIPRenderer.State state = new RoundRectangleFloatPIPRenderer.State(
                drawContext,
                x, y,
                width, height,
                backgroundColor, borderColor,
                borderRadius, borderWidth
        );
        
        GuiGraphicsHelper.submitPip(drawContext, state);
    }
}