package com.CoffeDino.lunacy.effects;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Lunacy.MODID)
public class SoakedEffect extends MobEffect {

    private static final float BONUS_PERCENT = 0.4f;
    private static final Set<LivingEntity> processing = Collections.newSetFromMap(new WeakHashMap<>());

    public SoakedEffect() {
        super(MobEffectCategory.HARMFUL, 0x1E90FF);
    }
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void handleLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide() || processing.contains(target)) return;
        if (!target.hasEffect(ModEffects.SOAKED)) return;

        DamageSource source = event.getSource();
        if (source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)) return;

        try {
            processing.add(target);
            float bonus = event.getNewDamage() * BONUS_PERCENT;
            if (bonus > 0) {
                target.hurt(target.damageSources().magic(), bonus);
                if (target.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SPLASH,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            6, 0.3, 0.3, 0.3, 0.05);
                }
            }
            target.removeEffect(ModEffects.SOAKED);
        } finally {
            processing.remove(target);
        }
    }
}