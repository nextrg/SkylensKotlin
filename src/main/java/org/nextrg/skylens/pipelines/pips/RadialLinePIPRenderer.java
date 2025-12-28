package org.nextrg.skylens.pipelines.pips;

import com.mojang.blaze3d.vertex.VertexFormat;
import earth.terrarium.olympus.client.pipelines.pips.OlympusPictureInPictureRenderState;
import earth.terrarium.olympus.client.pipelines.renderer.PipelineRenderer;
import earth.terrarium.olympus.client.utils.GuiGraphicsHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.nextrg.skylens.pipelines.RadialLine;
import org.nextrg.skylens.pipelines.uniforms.RadialLineUniform;

import java.util.function.Function;

public class RadialLinePIPRenderer extends SpecialGuiElementRenderer<RadialLinePIPRenderer.State> {
    public RadialLinePIPRenderer(VertexConsumerProvider.Immediate bufferSource) {
        super(bufferSource);
    }
    
    @Override
    public @NotNull Class<State> getElementClass() {
        return State.class;
    }
    
    @Override
    protected void render(State state, MatrixStack matrices) {
        final float scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        final float roundedRadius = Math.round(state.radius);
        final float paddedRadius = (roundedRadius + 4f) * scale;
        final float quadSize = paddedRadius * 2f;
        
        float xOffset = (state.fx - (float)state.x0 - roundedRadius) * scale;
        float yOffset = (state.fy - (float)state.y0 - roundedRadius) * scale;
        Vector2f center = new Vector2f(paddedRadius - xOffset, paddedRadius - yOffset);

        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        buffer.vertex(0f, 0f, 0f).color(state.color());
        buffer.vertex(0f, quadSize, 0f).color(state.color());
        buffer.vertex(quadSize, quadSize, 0f).color(state.color());
        buffer.vertex(quadSize, 0f, 0f).color(state.color());

        PipelineRenderer.builder(RadialLine.PIPELINE, buffer.end())
                .uniform(
                        RadialLineUniform.STORAGE,
                        RadialLineUniform.of(
                                state.lineColor,
                                center,
                                state.radius * scale,
                                state.startAngle + (float)(Math.PI / 2),
                                state.angleThickness,
                                state.fadeSoftness,
                                state.thickness / 2f,
                                state.mode
                        )
                )
                .draw();
    }
    
    @Override
    protected @NotNull String getName() {
        return "skylens_radial_line";
    }
    
    public static record State(
            int x0, int y0,
            int x1, int y1,
            int color,
            Vector4f lineColor,
            float radius,
            float startAngle,
            float angleThickness,
            float fadeSoftness,
            float thickness,
            int mode,
            float fx,
            float fy,
            Matrix3x2f pose,
            ScreenRect scissorArea,
            ScreenRect bounds
    ) implements OlympusPictureInPictureRenderState<State> {
        public State(
                DrawContext graphics,
                float x, float y,
                int color,
                Vector4f lineColor,
                float radius,
                float startAngle,
                float angleThickness,
                float fadeSoftness,
                float thickness,
                int mode
        ) {
            this(
                    (int)(Math.floor(x) - Math.round(radius) - 2.0),
                    (int)(Math.floor(y) - Math.round(radius) - 2.0),
                    (int)(Math.floor(x) + Math.round(radius) + 2.0),
                    (int)(Math.floor(y) + Math.round(radius) + 2.0),
                    color,
                    lineColor,
                    radius,
                    startAngle,
                    angleThickness,
                    fadeSoftness,
                    thickness,
                    mode,
                    x,
                    y,
                    new Matrix3x2f(graphics.getMatrices()),
                    GuiGraphicsHelper.getLastScissor(graphics),
                    SpecialGuiElementRenderState.createBounds(
                            (int)(Math.floor(x) - Math.round(radius) - 2.0),
                            (int)(Math.floor(y) - Math.round(radius) - 2.0),
                            (int)(Math.floor(x) + Math.round(radius) + 2.0),
                            (int)(Math.floor(y) + Math.round(radius) + 2.0),
                            GuiGraphicsHelper.getLastScissor(graphics)
                    )
            );
        }
        
        @Override
        public int x1() { return x0; }
        
        @Override
        public int y1() { return y0; }
        
        @Override
        public int x2() { return x1; }
        
        @Override
        public int y2() { return y1; }
        
        @Override
        public float scale() { return 1.0F; }
        
        @Override
        public Function<VertexConsumerProvider.Immediate, SpecialGuiElementRenderer<State>> getFactory() {
            return RadialLinePIPRenderer::new;
        }
        
        public int color() {
            return this.color;
        }
    }
}