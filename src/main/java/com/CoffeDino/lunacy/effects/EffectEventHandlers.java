package com.CoffeDino.lunacy.effects;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Lunacy.MODID)
public class EffectEventHandlers {

    private static Holder<MobEffect> MOURNING_FUNERAL_HOLDER = null;
    private static final Set<LivingEntity> processingEntities = Collections.newSetFromMap(new WeakHashMap<>());

    private static Holder<MobEffect> getMourningFuneralHolder() {
        if (MOURNING_FUNERAL_HOLDER == null) {
            MOURNING_FUNERAL_HOLDER = ModEffects.getMourningFuneralHolder();
        }
        return MOURNING_FUNERAL_HOLDER;
    }

    @SubscribeEvent
    public static void onLivingDamageBloodSurge(LivingDamageEvent.Post event) {
        BloodSurgeEffect.handleLivingDamage(event);
    }

    @SubscribeEvent
    public static void onLivingDamageEther(LivingDamageEvent.Post event) {
        EtherEffect.handleLivingDamage(event);
    }


    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDamageMourningFuneral(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();

        if (target.level().isClientSide()) return;
        if (processingEntities.contains(target)) {
            return;
        }

        MobEffectInstance effectInstance = target.getEffect(getMourningFuneralHolder());
        if (effectInstance != null) {
            try {
                processingEntities.add(target);

                int amplifier = effectInstance.getAmplifier();
                float damageAmount = 5.0f * (amplifier + 1);
                target.hurt(target.damageSources().magic(), damageAmount);

                if (target.level() instanceof ServerLevel serverLevel) {
                    MourningFuneralEffect.spawnHitParticles(target, serverLevel, amplifier);
                }
            } finally {
                processingEntities.remove(target);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTickMourningFuneral(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (entity.level().isClientSide()) return;
        if (entity.hasEffect(getMourningFuneralHolder()) &&
                entity.level() instanceof ServerLevel serverLevel &&
                entity.tickCount % 10 == 0) {

            MobEffectInstance effectInstance = entity.getEffect(getMourningFuneralHolder());
            if (effectInstance != null) {
                int amplifier = effectInstance.getAmplifier();
                MourningFuneralEffect.spawnAdditionalParticles(entity, serverLevel, amplifier);
            }
        }
    }
    @SubscribeEvent
    public static void onLivingTickSoulClaim(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (!entity.level().isClientSide() &&
                entity.hasEffect(ModEffects.SOUL_CLAIM) &&
                entity.tickCount % 40 == 0) {

            MobEffectInstance effectInstance = entity.getEffect(ModEffects.SOUL_CLAIM);
            if (effectInstance != null) {
                int amplifier = effectInstance.getAmplifier();
                float healAmount = 1.0f + amplifier;

                if (healAmount > 0) {
                    entity.heal(healAmount);

                    if (entity.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.SOUL,
                                entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                                4 + amplifier, 0.5, 0.5, 0.5, 0.05
                        );
                    }

                    if (entity.tickCount % 120 == 0) {
                        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 0.3F, 1.2F);
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void onLivingDamageEchoing(LivingIncomingDamageEvent event) {
        EchoingEffect.handleLivingDamage(event);
    }

    @SubscribeEvent
    public static void onMobEffectExpiredEchoing(MobEffectEvent.Expired event) {
        EchoingEffect.handleExpired(event);
    }
}