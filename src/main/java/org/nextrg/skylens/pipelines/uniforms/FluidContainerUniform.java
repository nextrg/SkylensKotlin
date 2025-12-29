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

public record FluidContainerUniform(
        Vector4f color,
        Vector4f radius,
        Vector2f size,
        Vector2f center,
        Vector2f offset,
        float scaleFactor,
        int waveDirection
) implements RenderPipelineUniforms {
    
    public static final String NAME = "FluidContainerUniform";
    
    public static final Supplier<DynamicUniformStorage<FluidContainerUniform>> STORAGE =
            Suppliers.<DynamicUniformStorage<FluidContainerUniform>>memoize(() -> new DynamicUniformStorage<>(
                    "Fluid Container UBO",
                    new Std140SizeCalculator()
                            .putVec4()
                            .putVec4()
                            .putVec2()
                            .putVec2()
                            .putVec2()
                            .putFloat()
                            .putInt()
                            .get(),
                    2
            ));
    
    public static FluidContainerUniform of(
            Vector4f color,
            Vector4f radius,
            Vector2f size,
            Vector2f center,
            Vector2f offset,
            float scaleFactor,
            int waveDirection
    ) {
        return new FluidContainerUniform(
                color,
                radius,
                size,
                center,
                offset,
                scaleFactor,
                waveDirection
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
                .putVec4(radius)
                .putVec2(size)
                .putVec2(center)
                .putVec2(offset)
                .putFloat(scaleFactor)
                .putInt(waveDirection)
                .get();
    }
}
