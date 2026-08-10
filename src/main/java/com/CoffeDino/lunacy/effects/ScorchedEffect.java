package com.CoffeDino.lunacy.effects;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
public class ScorchedEffect extends MobEffect {

    private static final float BONUS_PERCENT = 0.5f;
    private static final Set<LivingEntity> processing = Collections.newSetFromMap(new WeakHashMap<>());

    public ScorchedEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF7800);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void handleLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide() || processing.contains(target)) return;
        if (!target.hasEffect(ModEffects.SCORCHED)) return;

        DamageSource source = event.getSource();
        if (!(source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA))) return;

        try {
            processing.add(target);
            float bonus = event.getNewDamage() * BONUS_PERCENT;
            if (bonus > 0) {
                target.hurt(target.damageSources().magic(), bonus);
                if (target.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLAME,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            5, 0.3, 0.3, 0.3, 0.02);
                }
            }
        } finally {
            processing.remove(target);
        }
    }
}