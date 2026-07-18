package com.CoffeDino.lunacy.effects;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Lunacy.MODID);

    public static final ResourceKey<MobEffect> MOURNING_FUNERAL_KEY =
            ResourceKey.create(Registries.MOB_EFFECT,
                    ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "mourning_funeral"));

    public static final DeferredHolder<MobEffect, EtherEffect> ETHER = EFFECTS.register(
            "ether",
            EtherEffect::new
    );
    public static final DeferredHolder<MobEffect, BloodSurgeEffect> BLOOD_SURGE = EFFECTS.register(
            "blood_surge",
            BloodSurgeEffect::new
    );
    public static final DeferredHolder<MobEffect, MourningFuneralEffect> MOURNING_FUNERAL =
            EFFECTS.register("mourning_funeral", MourningFuneralEffect::new);
    public static final DeferredHolder<MobEffect, SoulClaimEffect> SOUL_CLAIM = EFFECTS.register(
            "soul_claim",
            SoulClaimEffect::new
    );
    public static final DeferredHolder<MobEffect, EchoingEffect> ECHOING = EFFECTS.register(
            "echoing",
            EchoingEffect::new
    );

    public static Holder<MobEffect> getMourningFuneralHolder() {
        return MOURNING_FUNERAL;
    }
}