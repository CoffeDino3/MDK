package com.CoffeDino.lunacy.effects;


import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EchoingEffect extends MobEffect {

    private static final Map<UUID, Float> accumulatedDamage = new HashMap<>();
    public static final int DURATION_TICKS = 200;
    private static final Map<UUID, Long> lastExpiredTick = new HashMap<>();
    private static final int REAPPLY_COOLDOWN_TICKS = 60;
    public EchoingEffect() {
        super(MobEffectCategory.HARMFUL, 0x9400D3);
    }

    public static boolean canApply(LivingEntity target) {
        if (accumulatedDamage.containsKey(target.getUUID())) return false;
        Long expiredTick = lastExpiredTick.get(target.getUUID());
        if (expiredTick != null) {
            long ticksSinceExpiry = target.level().getGameTime() - expiredTick;
            if (ticksSinceExpiry < REAPPLY_COOLDOWN_TICKS) return false;
        }
        return true;
    }
    public static void tryApply(LivingEntity target) {
        if (!canApply(target)) return;

        target.addEffect(new MobEffectInstance(
                ModEffects.ECHOING, DURATION_TICKS));
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);
        accumulatedDamage.put(entity.getUUID(), 0f);
    }

    public static void handleLivingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (accumulatedDamage.containsKey(entity.getUUID())) {
            accumulatedDamage.merge(entity.getUUID(), event.getAmount(), Float::sum);
            event.setAmount(0.0001f);
        }
    }


    public static void handleExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null) return;
        if (event.getEffectInstance().getEffect().value() != ModEffects.ECHOING.value()) return;

        LivingEntity entity = event.getEntity();
        Float total = accumulatedDamage.remove(entity.getUUID());
        lastExpiredTick.put(entity.getUUID(), entity.level().getGameTime());

        if (total != null && total > 0f) {
            float payback = total * 1.5f;
            entity.hurt(entity.damageSources().magic(), payback);

            if (entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                        entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                        1, 0.2, 0.2, 0.2, 0.0);
            }
        }
    }
}