package net.CoffeDino.testmod.effects;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, TestingCoffeDinoMod.MOD_ID);

    public static final RegistryObject<MobEffect> ETHER = EFFECTS.register(
            "ether",
            EtherEffect::new
    );
    public static final RegistryObject<MobEffect> BLOOD_SURGE = EFFECTS.register(
            "blood_surge",
            BloodSurgeEffect::new
    );
}