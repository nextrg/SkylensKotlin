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
import org.nextrg.skylens.pipelines.CircleChart;
import org.nextrg.skylens.pipelines.uniforms.CircleChartUniform;

import java.util.function.Function;

public class CircleChartPIPRenderer extends SpecialGuiElementRenderer<CircleChartPIPRenderer.State> {
    private State lastState;
    
    public CircleChartPIPRenderer(VertexConsumerProvider.Immediate bufferSource) {
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
    
    @Override
    protected void render(State state, MatrixStack matrices) {
        final float scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        final float roundedRadius = Math.round(state.outerRadius);
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

        PipelineRenderer.builder(CircleChart.PIPELINE, buffer.end())
                .uniform(
                        CircleChartUniform.STORAGE,
                        CircleChartUniform.of(
                                state.colors,
                                state.colorCount,
                                center,
                                state.outerRadius * scale,
                                state.innerRadius * scale,
                                state.progress,
                                state.time,
                                state.startAngle + (float)(Math.PI / 2),
                                state.reverse == 1,
                                state.invert == 1
                        )
                )
                .draw();
        this.lastState = state;
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
                    (int)(Math.floor(x) - Math.round(outerRadius) - 2.0),
                    (int)(Math.floor(y) - Math.round(outerRadius) - 2.0),
                    (int)(Math.floor(x) + Math.round(outerRadius) + 2.0),
                    (int)(Math.floor(y) + Math.round(outerRadius) + 2.0),
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
                    x,
                    y,
                    new Matrix3x2f(graphics.getMatrices()),
                    GuiGraphicsHelper.getLastScissor(graphics),
                    SpecialGuiElementRenderState.createBounds(
                            (int)(Math.floor(x) - Math.round(outerRadius) - 2.0),
                            (int)(Math.floor(y) - Math.round(outerRadius) - 2.0),
                            (int)(Math.floor(x) + Math.round(outerRadius) + 2.0),
                            (int)(Math.floor(y) + Math.round(outerRadius) + 2.0),
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
            return CircleChartPIPRenderer::new;
        }
        
        public int color() {
            return this.color;
        }
    }
}