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
    public CircleChartPIPRenderer(VertexConsumerProvider.Immediate bufferSource) {
        super(bufferSource);
    }
    
    @Override
    public @NotNull Class<State> getElementClass() {
        return State.class;
    }
    
    @Override
    protected void render(State state, MatrixStack matrices) {
        var window = MinecraftClient.getInstance().getWindow();
        float scale = (float) window.getScaleFactor();
        
        float padding = 4f;
        float size = (Math.round(state.outerRadius) + padding) * 2 * scale;
        
        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        buffer.vertex(0.0F, 0.0F, 0.0F).color(state.color());
        buffer.vertex(0.0F, size, 0.0F).color(state.color());
        buffer.vertex(size, size, 0.0F).color(state.color());
        buffer.vertex(size, 0.0F, 0.0F).color(state.color());
        
        Vector2f center = getCenter(state, scale, padding);
        
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
                                state.startAngle,
                                state.reverse == 1,
                                state.invert == 1
                        )
                )
                .draw();
    }
    
    public static Vector2f getCenter(State state, float scale, float padding) {
        float roundedRadius = Math.round(state.outerRadius);
        float halfSize = (roundedRadius + padding) * scale;
        
        float xOffset = state.fx - (float)state.x0 - roundedRadius;
        float yOffset = state.fy - (float)state.y0 - roundedRadius;
        float xCenter = halfSize - xOffset * scale;
        float yCenter = halfSize - yOffset * scale;
        return new Vector2f(xCenter, yCenter);
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
                    (int)Math.floor(Math.round(x) - Math.round(outerRadius) - 2.0),
                    (int)Math.floor(Math.round(y) - Math.round(outerRadius) - 2.0),
                    (int)Math.ceil(Math.round(x) + Math.round(outerRadius) + 2.0),
                    (int)Math.ceil(Math.round(y) + Math.round(outerRadius) + 2.0),
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
                            (int)(x - Math.round(outerRadius) - 2.0),
                            (int)(y - Math.round(outerRadius) - 2.0),
                            (int)(x + Math.round(outerRadius) + 2.0),
                            (int)(y + Math.round(outerRadius) + 2.0),
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