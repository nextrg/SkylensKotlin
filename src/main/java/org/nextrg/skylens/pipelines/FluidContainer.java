package org.nextrg.skylens.pipelines;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.shaders.UniformType;
import earth.terrarium.olympus.client.utils.fabric.GuiGraphicsHelperImpl;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.nextrg.skylens.Skylens;
import org.nextrg.skylens.pipelines.pips.FluidContainerPIPRenderer;
import org.nextrg.skylens.pipelines.uniforms.FluidContainerUniform;

public class FluidContainer {
    public static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Skylens.Companion.id("core/fluid_container"))
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("FluidContainerUniform", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withFragmentShader(Skylens.Companion.id("core/fluid_container"))
            .withVertexShader(Skylens.Companion.id("core/basic_transform"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();
    
    /**
     * Draws a fake fluid simulation in a 2D rectangle with rounded corners.
     * @param guiGraphics Context used to draw the ui element
     * @param x X position of <b>left</b> corner of the rectangle
     * @param y Y position of <b>top</b> corner of the rectangle
     * @param fillColor Color of the fluid
     * @param waveDirection Side that gets filled with the fluid (0 → top, 1 → bottom, 2 → left, 3 → right)
     * @param offset Offset of the fluid, 100f → full
     * @param borderRadius Border radius of the rectangle with the fluid
     *
     * @see FluidContainerUniform
     * @see FluidContainerPIPRenderer
     */
    public static void draw(GuiGraphics guiGraphics, float x, float y, float width, float height, Vector4f fillColor, int waveDirection, Vector2f offset, float borderRadius) {
        FluidContainerPIPRenderer.State state = new FluidContainerPIPRenderer.State(
                guiGraphics,
                x, y,
                width, height,
                0xFFFFFFFF,
                fillColor,
                waveDirection,
                offset,
                borderRadius
        );

        GuiGraphicsHelperImpl.submitPip(guiGraphics, state);
    }
}
