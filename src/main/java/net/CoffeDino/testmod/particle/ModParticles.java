package net.CoffeDino.testmod.particle;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TestingCoffeDinoMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> MOURNING_BUTTERFLY_PARTICLES =
            PARTICLE_TYPES.register("mourning_butterfly_particles",
                    () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> CLOCK_PARTICLES =
            PARTICLE_TYPES.register("clock_particles",
                    () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus){
        PARTICLE_TYPES.register(eventBus);
    }
}
