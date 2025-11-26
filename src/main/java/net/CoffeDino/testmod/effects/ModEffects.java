package net.CoffeDino.testmod.effects;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Lunacy.MOD_ID);

    public static final ResourceKey<MobEffect> MOURNING_FUNERAL_KEY =
            ResourceKey.create(Registries.MOB_EFFECT,
                    ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "mourning_funeral"));

    public static final RegistryObject<MobEffect> ETHER = EFFECTS.register(
            "ether",
            EtherEffect::new
    );
    public static final RegistryObject<MobEffect> BLOOD_SURGE = EFFECTS.register(
            "blood_surge",
            BloodSurgeEffect::new
    );
    public static final RegistryObject<MobEffect> MOURNING_FUNERAL =
            EFFECTS.register("mourning_funeral", MourningFuneralEffect::new);

    public static Holder<MobEffect> getMourningFuneralHolder() {
        return MOURNING_FUNERAL.getHolder().orElseThrow();
    }
}