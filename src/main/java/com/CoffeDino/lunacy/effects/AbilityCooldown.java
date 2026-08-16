package com.CoffeDino.lunacy.effects;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class AbilityCooldown {
    private AbilityCooldown() {}

    public static boolean isActive(LivingEntity entity, Holder<MobEffect> cooldown) {
        return entity.hasEffect(cooldown);
    }
    public static void start(LivingEntity entity, Holder<MobEffect> cooldown, int durationTicks) {
        entity.addEffect(new MobEffectInstance(cooldown, durationTicks, 0, false, false, true));
    }
}