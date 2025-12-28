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
import org.nextrg.skylens.pipelines.FluidContainer;
import org.nextrg.skylens.pipelines.uniforms.FluidContainerUniform;

import java.util.function.Function;

public class FluidContainerPIPRenderer extends SpecialGuiElementRenderer<FluidContainerPIPRenderer.State> {
    private State lastState;
    
    public FluidContainerPIPRenderer(VertexConsumerProvider.Immediate bufferSource) {
        super(bufferSource);
    }
    
    @Override
    public @NotNull Class<State> getElementClass() {
        return State.class;
    }
    
    @Override
    protected boolean shouldBypassScaling(State state) {
        return lastState != null && lastState.equals(state);
    }
    
    protected void render(State state, MatrixStack stack) {
        final float scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        final float paddedX = 4f * scale;
        final float scaledWidth = (state.x1 - state.x0) * scale;
        final float scaledHeight = (state.y1 - state.y0) * scale;

        final Vector2f size = new Vector2f(scaledWidth - paddedX, scaledHeight - paddedX);
        final float offsetX = (state.fx - 2.0f - state.x0) * scale;
        final float offsetY = (state.fy - 2.0f - state.y0) * scale;
        final Vector2f center = new Vector2f(size.x / 2f + paddedX / 2f - offsetX, size.y / 2f + paddedX / 2f - offsetY);
        
        Vector4f radius = new Vector4f(state.borderRadius);
        
        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        buffer.vertex(0.0f, 0.0f, 0.0f).color(state.color());
        buffer.vertex(0.0f, scaledHeight, 0.0f).color(state.color());
        buffer.vertex(scaledWidth, scaledHeight, 0.0f).color(state.color());
        buffer.vertex(scaledWidth, 0.0f, 0.0f).color(state.color());
        
        PipelineRenderer.builder(FluidContainer.PIPELINE, buffer.end())
                .uniform(FluidContainerUniform.STORAGE,
                        FluidContainerUniform.of(
                                state.fillColor,
                                radius,
                                size,
                                center,
                                state.offset,
                                scale,
                                state.waveDirection
                        ))
                .draw();
        this.lastState = state;
    }
    
    protected @NotNull String getName() {
        return "skylens_fluid_container";
    }
    
    public static record State(
            int x0, int y0,
            int x1, int y1,
            int color,
            Vector4f fillColor,
            int waveDirection,
            Vector2f offset,
            float borderRadius,
            float fx,
            float fy,
            Matrix3x2f pose,
            ScreenRect scissorArea,
            ScreenRect bounds
    ) implements OlympusPictureInPictureRenderState<State> {
        public State(
                DrawContext graphics,
                float x,
                float y,
                float width,
                float height,
                int color,
                Vector4f fillColor,
                int waveDirection,
                Vector2f offset,
                float borderRadius) {
            this(
                    (int)Math.floor(x - 2.0),
                    (int)Math.floor(y - 2.0),
                    (int)Math.ceil(x + width + 2.0),
                    (int)Math.ceil(y + height + 2.0),
                    color,
                    fillColor,
                    waveDirection,
                    offset,
                    borderRadius,
                    x,
                    y,
                    new Matrix3x2f(graphics.getMatrices()),
                    GuiGraphicsHelper.getLastScissor(graphics),
                    SpecialGuiElementRenderState.createBounds(
                            (int)Math.floor(x - 2.0),
                            (int)Math.floor(y - 2.0),
                            (int)Math.ceil(x + width + 2.0),
                            (int)Math.ceil(y + height + 2.0),
                            GuiGraphicsHelper.getLastScissor(graphics)
                    )
            );
        }
        
        public float scale() {
            return 1.0f;
        }
        
        public Function<VertexConsumerProvider.Immediate, SpecialGuiElementRenderer<State>> getFactory() {
            return FluidContainerPIPRenderer::new;
        }
        
        public int x1() {
            return this.x0;
        }
        
        public int y1() {
            return this.y0;
        }
        
        public int x2() {
            return this.x1;
        }
        
        public int y2() {
            return this.y1;
        }
        
        public int color() {
            return this.color;
        }
        
        public float borderRadius() {
            return this.borderRadius;
        }
        
        public Matrix3x2f pose() {
            return this.pose;
        }
        
        public ScreenRect scissorArea() {
            return this.scissorArea;
        }
        
        public ScreenRect bounds() {
            return this.bounds;
        }
    }
}
