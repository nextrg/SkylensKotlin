package org.nextrg.skylens.pipelines.uniforms;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import earth.terrarium.olympus.client.pipelines.uniforms.RenderPipelineUniforms;
import net.minecraft.client.renderer.DynamicUniformStorage;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public record RadialLineUniform(
        Vector4f color,
        Vector2f center,
        float radius,
        float startAngle,
        float angleThickness,
        float fadeSoftness,
        float thickness,
        int mode
) implements RenderPipelineUniforms {
    
    public static final String NAME = "RadialLineUniform";
    
    public static final Supplier<DynamicUniformStorage<RadialLineUniform>> STORAGE =
            Suppliers.<DynamicUniformStorage<RadialLineUniform>>memoize(() -> new DynamicUniformStorage<>(
                    "Radial Line UBO",
                    new Std140SizeCalculator()
                            .putVec4()
                            .putVec2()
                            .putFloat()
                            .putFloat()
                            .putFloat()
                            .putFloat()
                            .putFloat()
                            .putInt()
                            .get(),
                    2
            ));
    
    public static RadialLineUniform of(
            Vector4f color,
            Vector2f center,
            float radius,
            float startAngle,
            float angleThickness,
            float fadeSoftness,
            float thickness,
            int mode
    ) {
        return new RadialLineUniform(
                color,
                center,
                radius,
                startAngle,
                angleThickness,
                fadeSoftness,
                thickness,
                mode
        );
    }
    
    @Override
    public String name() {
        return NAME;
    }
    
    @Override
    public void write(ByteBuffer buffer) {
        Std140Builder builder = Std140Builder.intoBuffer(buffer);
        builder.putVec4(color)
                .putVec2(center)
                .putFloat(radius)
                .putFloat(startAngle)
                .putFloat(angleThickness)
                .putFloat(fadeSoftness)
                .putFloat(thickness)
                .putInt(mode)
                .get();
    }
}
