package org.nextrg.skylens.pipelines.pips;

import com.mojang.blaze3d.vertex.VertexFormat;
import earth.terrarium.olympus.client.pipelines.pips.OlympusPictureInPictureRenderState;
import earth.terrarium.olympus.client.pipelines.renderer.PipelineRenderer;
import earth.terrarium.olympus.client.pipelines.uniforms.RoundedRectangleUniform;
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
import org.nextrg.skylens.helpers.VariablesUtil;
import org.nextrg.skylens.pipelines.RoundRectangleFloat;

import java.util.function.Function;

public class RoundRectangleFloatPIPRenderer extends SpecialGuiElementRenderer<RoundRectangleFloatPIPRenderer.State> {
    private State lastState;
    
    public RoundRectangleFloatPIPRenderer(VertexConsumerProvider.Immediate bufferSource) {
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
        
        final Vector2f size = new Vector2f(state.fwidth * scale, state.fheight * scale);
        final float fWidth = size.x + paddedX;
        final float fHeight = size.y + paddedX;
        
        final Vector2f center = new Vector2f(fWidth * 0.5f, fHeight * 0.5f);
        
        final Vector4f radius = new Vector4f(state.borderRadius);
        final Vector4f borderColor = VariablesUtil.INSTANCE.intToVector4f(state.borderColor());
 
        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(0.0f, 0.0f, 0.0f).color(state.color());
        buffer.vertex(0.0f, fHeight, 0.0f).color(state.color());
        buffer.vertex(fWidth, fHeight, 0.0f).color(state.color());
        buffer.vertex(fWidth, 0.0f, 0.0f).color(state.color());
        
        PipelineRenderer.builder(RoundRectangleFloat.PIPELINE, buffer.end())
                .uniform(RoundedRectangleUniform.STORAGE,
                        RoundedRectangleUniform.of(
                                borderColor,
                                radius,
                                state.borderWidth(),
                                size,
                                center,
                                scale
                        ))
                .draw();
        this.lastState = state;
    }
    
    protected @NotNull String getName() {
        return "skylens_round_rectangle_float";
    }
    
    public static record State(
            int x0, int y0,
            int x1, int y1,
            int color,
            int borderColor,
            float borderRadius,
            float borderWidth,
            float fx,
            float fy,
            float fwidth,
            float fheight,
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
                int borderColor,
                float borderRadius,
                float borderWidth) {
            this(
                    (int)Math.floor(x - 2.0),
                    (int)Math.floor(y - 2.0),
                    (int)Math.ceil(x + width + 2.0),
                    (int)Math.ceil(y + height + 2.0),
                    color,
                    borderColor,
                    borderRadius,
                    borderWidth,
                    x,
                    y,
                    width,
                    height,
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
            return RoundRectangleFloatPIPRenderer::new;
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
        
        public int borderColor() {
            return this.borderColor;
        }
        
        public float borderRadius() {
            return this.borderRadius;
        }
        
        public float borderWidth() {
            return this.borderWidth;
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
