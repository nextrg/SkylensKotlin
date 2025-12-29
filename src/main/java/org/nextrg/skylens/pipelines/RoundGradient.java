package org.nextrg.skylens.pipelines;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.shaders.UniformType;
import earth.terrarium.olympus.client.utils.fabric.GuiGraphicsHelperImpl;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
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
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();
    
    /**
     * Draws a 2D rectangle with rounded corners on UI using Olympus rounded rectangle, but with float variables.
     * @param guiGraphics Context used to draw the ui element
     * @param x X position of <b>left</b> corner of the rectangle
     * @param y Y position of <b>top</b> corner of the rectangle
     *
     * @see RoundGradientUniform
     * @see RoundGradientPIPRenderer
     */
    public static void draw(GuiGraphics guiGraphics, float x, float y, float width, float height, Vector4f[] colors, float time, int gradientDirection, int borderColor, float borderRadius, float borderWidth) {
        RoundGradientPIPRenderer.State state = new RoundGradientPIPRenderer.State(
                guiGraphics,
                x, y,
                width, height,
                0xFFFFFFFF,
                colors, colors.length, time,
                gradientDirection,
                borderColor, borderRadius, borderWidth
        );

        GuiGraphicsHelperImpl.submitPip(guiGraphics, state);
    }
}
