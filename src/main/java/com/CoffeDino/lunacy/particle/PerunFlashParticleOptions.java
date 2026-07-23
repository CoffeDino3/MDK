package com.CoffeDino.lunacy.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PerunFlashParticleOptions implements ParticleOptions {
    private final ParticleType<PerunFlashParticleOptions> type;

    public PerunFlashParticleOptions(float scale, ParticleType<PerunFlashParticleOptions> type) {
        this.scale = scale;
        this.type = type;
    }

    public PerunFlashParticleOptions(float scale) {
        this(scale, ModParticles.PERUN_FLASH.get());
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    public static final MapCodec<PerunFlashParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("scale").forGetter(PerunFlashParticleOptions::getScale)
            ).apply(instance, PerunFlashParticleOptions::new)
    );

    public static final StreamCodec<ByteBuf, PerunFlashParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, PerunFlashParticleOptions::getScale,
                    PerunFlashParticleOptions::new
            );

    private final float scale;


    public float getScale() {
        return scale;
    }

}