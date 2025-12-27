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
import org.joml.Vector2f;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Vector4f;
import org.nextrg.skylens.pipelines.CircleChart;
import org.nextrg.skylens.pipelines.uniforms.CircleChartUniform;

import java.util.function.Function;

public class CircleChartPIPRenderer extends SpecialGuiElementRenderer<CircleChartPIPRenderer.State> {
    public CircleChartPIPRenderer(VertexConsumerProvider.Immediate bufferSource) {
        super(bufferSource);
    }
    
    @Override
    public @NotNull Class<State> getElementClass() {
        return State.class;
    }
    
    @Override
    protected void render(State state, MatrixStack matrices) {
        float scale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
        float size = state.outerRadius * 2 * scale;
        
        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        buffer.vertex(0.0F, 0.0F, 0.0F).color(state.color());
        buffer.vertex(0.0F, size, 0.0F).color(state.color());
        buffer.vertex(size, size, 0.0F).color(state.color());
        buffer.vertex(size, 0.0F, 0.0F).color(state.color());
        
        PipelineRenderer.builder(CircleChart.PIPELINE, buffer.end())
                .uniform(
                        CircleChartUniform.STORAGE,
                        CircleChartUniform.of(
                                state.colors,
                                state.colorCount,
                                new Vector2f(
                                        size / 2.0F,
                                        size / 2.0F
                                ),
                                state.outerRadius * scale,
                                state.innerRadius * scale,
                                state.progress,
                                state.time,
                                state.startAngle,
                                state.reverse == 1,
                                state.invert == 1
                        )
                )
                .draw();
    }
    
    @Override
    protected @NotNull String getName() {
        return "skylens_circle_chart";
    }
    
    public static record State(
            int x0, int y0,
            int x1, int y1,
            int color,
            Vector4f[] colors,
            int colorCount,
            float outerRadius,
            float innerRadius,
            float progress,
            float time,
            float startAngle,
            int reverse,
            int invert,
            Matrix3x2f pose,
            ScreenRect scissorArea,
            ScreenRect bounds
    ) implements OlympusPictureInPictureRenderState<State> {
        public State(
                DrawContext graphics,
                float x, float y,
                int color,
                Vector4f[] colors,
                int colorCount,
                float outerRadius,
                float innerRadius,
                float progress,
                float time,
                float startAngle,
                boolean reverse,
                boolean invert
        ) {
            this(
                    (int)x, (int)y, (int)(x + outerRadius * 2), (int)(y + outerRadius * 2),
                    color,
                    colors,
                    colorCount,
                    outerRadius,
                    innerRadius,
                    progress,
                    time,
                    startAngle,
                    reverse ? 1 : 0,
                    invert ? 1 : 0,
                    new Matrix3x2f(graphics.getMatrices()),
                    GuiGraphicsHelper.getLastScissor(graphics),
                    SpecialGuiElementRenderState.createBounds(
                            (int)x, (int)y, (int)(x + outerRadius * 2), (int)(y + outerRadius * 2),
                            GuiGraphicsHelper.getLastScissor(graphics)
                    )
            );
        }
        
        @Override
        public int x1() { return (int)x0; }
        
        @Override
        public int y1() { return (int)y0; }
        
        @Override
        public int x2() { return (int)(x0 + outerRadius * 2); }
        
        @Override
        public int y2() { return (int)(y0 + outerRadius * 2); }
        
        @Override
        public float scale() {
            return 1.0F;
        }
        
        @Override
        public Function<VertexConsumerProvider.Immediate, SpecialGuiElementRenderer<State>> getFactory() {
            return CircleChartPIPRenderer::new;
        }
        
        public int color() {
            return this.color;
        }
    }
}