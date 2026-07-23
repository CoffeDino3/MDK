package com.CoffeDino.lunacy.particle;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Lunacy.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MOURNING_BUTTERFLY_PARTICLES =
            PARTICLE_TYPES.register("mourning_butterfly_particles",
                    () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CLOCK_PARTICLES =
            PARTICLE_TYPES.register("clock_particles",
                    () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, PerunFlashParticleType> PERUN_FLASH =
            PARTICLE_TYPES.register("perun_flash", () -> new PerunFlashParticleType(false));



    public static void register(IEventBus eventBus){
        PARTICLE_TYPES.register(eventBus);
    }
}