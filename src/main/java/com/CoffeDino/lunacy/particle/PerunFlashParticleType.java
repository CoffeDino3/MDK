package com.CoffeDino.lunacy.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class PerunFlashParticleType extends ParticleType<PerunFlashParticleOptions> {

    public PerunFlashParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public MapCodec<PerunFlashParticleOptions> codec() {
        return PerunFlashParticleOptions.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, PerunFlashParticleOptions> streamCodec() {
        return PerunFlashParticleOptions.STREAM_CODEC;
    }
}