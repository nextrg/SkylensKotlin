package org.nextrg.skylens.pipelines.uniforms;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import earth.terrarium.olympus.client.pipelines.uniforms.RenderPipelineUniforms;
import net.minecraft.client.gl.DynamicUniformStorage;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public record RoundGradientUniform(
        Vector4f[] colors,
        int colorCount,
        Vector4f borderColor,
        Vector4f borderRadius,
        Vector2f size,
        Vector2f center,
        float borderWidth,
        float scaleFactor,
        float time,
        int gradientDir
) implements RenderPipelineUniforms {
    
    public static final String NAME = "RoundGradientUniform";
    
    public static final Supplier<DynamicUniformStorage<RoundGradientUniform>> STORAGE =
            Suppliers.<DynamicUniformStorage<RoundGradientUniform>>memoize(() -> new DynamicUniformStorage<>(
                    "Round Gradient UBO",
                    new Std140SizeCalculator()
                            .putVec4() // color 0
                            .putVec4() // color 1
                            .putVec4() // color 2
                            .putVec4() // color 3
                            .putVec4() // color 4
                            .putVec4() // color 5
                            .putVec4() // color 6
                            .putVec4() // color 7
                            .putInt()
                            .putVec4()
                            .putVec4()
                            .putVec2()
                            .putVec2()
                            .putFloat()
                            .putFloat()
                            .putFloat()
                            .putInt()
                            .get(),
                    2
            ));
    
    public static RoundGradientUniform of(
            Vector4f[] colors,
            int colorCount,
            Vector4f borderColor,
            Vector4f borderRadius,
            Vector2f size,
            Vector2f center,
            float borderWidth,
            float scaleFactor,
            float time,
            int gradientDir
    ) {
        return new RoundGradientUniform(
                colors,
                colorCount,
                borderColor,
                borderRadius,
                size,
                center,
                borderWidth,
                scaleFactor,
                time,
                gradientDir
        );
    }
    
    @Override
    public String name() {
        return NAME;
    }
    
    @Override
    public void write(ByteBuffer buffer) {
        Std140Builder builder = Std140Builder.intoBuffer(buffer);
        for (int i = 0; i < 8; i++) {
            builder.putVec4(i < colors.length ? colors[i] : new Vector4f(0,0,0,0));
        }
        builder.putInt(colorCount)
                .putVec4(borderColor)
                .putVec4(borderRadius)
                .putVec2(size)
                .putVec2(center)
                .putFloat(borderWidth)
                .putFloat(scaleFactor)
                .putFloat(time)
                .putInt(gradientDir)
                .get();
    }
}