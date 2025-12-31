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

import static org.nextrg.skylens.helpers.VariablesUtil.getFloatCenter;

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

        final Vector2f size = new Vector2f(state.fwidth * scale, state.fheight * scale);
        final Vector2f center = getFloatCenter(state, new Vector2f(state.fx0, state.fx1), new Vector2f(state.fy0, state.fy1), scale);
        final Vector4f radius = state.borderRadius;
        final Vector4f borderColor = VariablesUtil.INSTANCE.intToVector4f(state.borderColor());
 
        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        buffer.vertex(-4f, -4f, 0f).color(state.color());
        buffer.vertex(-4f, size.y + 4f, 0f).color(state.color());
        buffer.vertex(size.x + 4f, size.y + 4f, 0f).color(state.color());
        buffer.vertex(size.x + 4f, -4f, 0f).color(state.color());
        
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
            Vector4f borderRadius,
            float borderWidth,
            float fx0,
            float fy0,
            float fx1,
            float fy1,
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
                Vector4f borderRadius,
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
                    x - 2f,
                    y - 2f,
                    x + width + 2f,
                    y + height + 2f,
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
        
        public Vector4f borderRadius() {
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
