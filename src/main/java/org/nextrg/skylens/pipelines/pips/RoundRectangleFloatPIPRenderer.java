package org.nextrg.skylens.pipelines.pips;

import com.mojang.blaze3d.vertex.VertexFormat;
import earth.terrarium.olympus.client.pipelines.pips.OlympusPictureInPictureRenderState;
import earth.terrarium.olympus.client.pipelines.renderer.PipelineRenderer;
import earth.terrarium.olympus.client.pipelines.uniforms.RoundedRectangleUniform;
import earth.terrarium.olympus.client.utils.GuiGraphicsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.nextrg.skylens.helpers.VariablesUtil;
import org.nextrg.skylens.pipelines.RoundRectangleFloat;

import java.util.function.Function;

import static org.nextrg.skylens.helpers.VariablesUtil.getFloatCenter;

public class RoundRectangleFloatPIPRenderer extends PictureInPictureRenderer<RoundRectangleFloatPIPRenderer.State> {
    private State lastState;
    
    public RoundRectangleFloatPIPRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }
    
    @Override
    public @NotNull Class<State> getRenderStateClass() {
        return State.class;
    }
    
    @Override
    protected boolean textureIsReadyToBlit(State state) {
        return lastState != null && lastState.equals(state);
    }
    
    protected void renderToTexture(State state, PoseStack pose) {
        final float scale = Minecraft.getInstance().getWindow().getGuiScale();
        
        final Vector2f size = new Vector2f(state.fwidth * scale, state.fheight * scale);
        final Vector2f center = getFloatCenter(state, new Vector2f(state.fx0, state.fx1), new Vector2f(state.fy0, state.fy1), scale);
        final Vector4f radius = new Vector4f(state.borderRadius);
        final Vector4f borderColor = VariablesUtil.INSTANCE.intToVector4f(state.borderColor());
        
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        buffer.addVertex(-4f, -4f, 0f).setColor(state.color());
        buffer.addVertex(-4f, size.y + 4f, 0f).setColor(state.color());
        buffer.addVertex(size.x + 4f, size.y + 4f, 0f).setColor(state.color());
        buffer.addVertex(size.x + 4f, -4f, 0f).setColor(state.color());
        
        PipelineRenderer.builder(RoundRectangleFloat.PIPELINE, buffer.buildOrThrow())
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
    
    protected @NotNull String getTextureLabel() {
        return "skylens_round_rectangle_float";
    }
    
    public static record State(
            int x0, int y0,
            int x1, int y1,
            int color,
            int borderColor,
            float borderRadius,
            float borderWidth,
            float fx0,
            float fy0,
            float fx1,
            float fy1,
            float fwidth,
            float fheight,
            Matrix3x2f pose,
            ScreenRectangle scissorArea,
            ScreenRectangle bounds
    ) implements OlympusPictureInPictureRenderState<State> {
        public State(
                GuiGraphics graphics,
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
                    x - 2f,
                    y - 2f,
                    x + width + 2f,
                    y + height + 2f,
                    width,
                    height,
                    new Matrix3x2f(graphics.pose()),
                    GuiGraphicsHelper.getLastScissor(graphics),
                    PictureInPictureRenderState.getBounds(
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
        
        public Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<State>> getFactory() {
            return RoundRectangleFloatPIPRenderer::new;
        }

        public int x0() {
            return this.x0;
        }

        public int y0() {
            return this.y0;
        }

        public int x1() {
            return this.x1;
        }

        public int y1() {
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
        
        public ScreenRectangle scissorArea() {
            return this.scissorArea;
        }
        
        public ScreenRectangle bounds() {
            return this.bounds;
        }
    }
}
