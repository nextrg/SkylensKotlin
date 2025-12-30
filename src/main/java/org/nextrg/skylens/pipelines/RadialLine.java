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
import org.nextrg.skylens.pipelines.pips.RadialLinePIPRenderer;
import org.nextrg.skylens.pipelines.uniforms.RadialLineUniform;

public class RadialLine {
    public static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Skylens.Companion.id("radial_line"))
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("RadialLineUniform", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withFragmentShader(Skylens.Companion.id("core/radial_line"))
            .withVertexShader(Skylens.Companion.id("core/basic_transform"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();
    
    /**
     * Draws a line rotating around a sphere with given radius
     * @param guiGraphics Context used to draw the ui element
     * @param x X position of <b>left</b> corner of the circle
     * @param y Y position of <b>top</b> corner of the circle
     * @param radius Radius of the sphere where the line is rendered on
     * @param color Color of the line
     * @param startAngle Angle of the line (by default at the top)
     * @param mode Line alpha mode (0 → angle, 1 → thickness, 2 → both)
     *
     * @see RadialLinePIPRenderer
     * @see RadialLineUniform
     */
    public static void draw(GuiGraphics guiGraphics, float x, float y, Vector4f color, float radius, float startAngle, float angleThickness, float fadeSoftness, float thickness, int mode) {
        RadialLinePIPRenderer.State state = new RadialLinePIPRenderer.State(
                guiGraphics,
                x + 2f, y + 2f,
                0xFFFFFFFF, color,
                radius, startAngle,
                angleThickness, fadeSoftness,
                thickness, mode
        );

        GuiGraphicsHelperImpl.submitPip(guiGraphics, state);
    }
}
