package com.CoffeDino.lunacy.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class LeadPoisoningEffect extends MobEffect {

    private static final int TICK_INTERVAL = 20;
    private static final int APPLICATIONS = 20;
    private static final float TOTAL_DAMAGE_PERCENT = 0.10f;

    public LeadPoisoningEffect() {
        super(MobEffectCategory.HARMFUL, 0x4B5320);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % TICK_INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        float damagePerTick = entity.getMaxHealth() * TOTAL_DAMAGE_PERCENT / APPLICATIONS;
        entity.hurt(entity.damageSources().magic(), damagePerTick);
        return true;
    }
}