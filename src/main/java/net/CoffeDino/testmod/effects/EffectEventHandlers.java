package net.CoffeDino.testmod.effects;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
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
    public static void onLivingDamageBloodSurge(LivingDamageEvent event) {
        BloodSurgeEffect.handleLivingDamage(event);
    }

    @SubscribeEvent
    public static void onLivingDamageEther(LivingDamageEvent event) {
        EtherEffect.handleLivingDamage(event);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDamageMourningFuneral(LivingDamageEvent event) {
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
                float damageAmount = 10.0f * (amplifier + 1);
                target.hurt(target.damageSources().magic(), damageAmount);

                if (target.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    MourningFuneralEffect.spawnHitParticles(target, serverLevel, amplifier);
                }
            } finally {
                processingEntities.remove(target);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTickMourningFuneral(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) return;
        if (entity.hasEffect(getMourningFuneralHolder()) &&
                entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel &&
                entity.tickCount % 10 == 0) {

            MobEffectInstance effectInstance = entity.getEffect(getMourningFuneralHolder());
            if (effectInstance != null) {
                int amplifier = effectInstance.getAmplifier();
                MourningFuneralEffect.spawnAdditionalParticles(entity, serverLevel, amplifier);
            }
        }
    }
}