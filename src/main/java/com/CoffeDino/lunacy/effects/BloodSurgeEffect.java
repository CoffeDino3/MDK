package com.CoffeDino.lunacy.effects;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import com.CoffeDino.lunacy.Lunacy;

@EventBusSubscriber(modid = Lunacy.MODID)
public class BloodSurgeEffect extends MobEffect {
    public BloodSurgeEffect() {
        super(
                MobEffectCategory.BENEFICIAL,
                0x8B0000
        );
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void handleLivingDamage(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();

        if (source.getEntity() instanceof LivingEntity attacker &&
                attacker.hasEffect(ModEffects.BLOOD_SURGE) &&
                !attacker.level().isClientSide() &&
                target != attacker) {

            float damage = event.getNewDamage();
            MobEffectInstance effectInstance = attacker.getEffect(ModEffects.BLOOD_SURGE);
            int amplifier = effectInstance != null ? effectInstance.getAmplifier() : 0;
            float healPercent = 0.20f + (amplifier * 0.05f);
            float healAmount = damage * healPercent;

            if (healAmount > 0) {
                attacker.heal(healAmount);
                if (attacker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.DAMAGE_INDICATOR,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            5, 0.3, 0.3, 0.3, 0.1
                    );
                    if (attacker instanceof Player) {
                        serverLevel.sendParticles(
                                ParticleTypes.HEART,
                                attacker.getX(), attacker.getY() + 1.5, attacker.getZ(),
                                1, 0.2, 0.2, 0.2, 0.05
                        );
                    } else {
                        serverLevel.sendParticles(
                                ParticleTypes.HAPPY_VILLAGER,
                                attacker.getX(), attacker.getY() + attacker.getBbHeight() * 0.5, attacker.getZ(),
                                3, 0.3, 0.3, 0.3, 0.05
                        );
                    }
                    spawnBloodFlowParticles(target, attacker, serverLevel);
                }
                attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                        SoundEvents.PLAYER_HURT,
                        SoundSource.NEUTRAL, 0.7F, 0.8F);
            }
        }
    }

    private static void spawnBloodFlowParticles(LivingEntity from, LivingEntity to, ServerLevel level) {
        int particles = 8;
        double startX = from.getX();
        double startY = from.getY() + from.getBbHeight() * 0.5;
        double startZ = from.getZ();

        double endX = to.getX();
        double endY = to.getY() + to.getBbHeight() * 0.5;
        double endZ = to.getZ();

        for (int i = 0; i < particles; i++) {
            double progress = (double) i / particles;
            double x = startX + (endX - startX) * progress + (level.random.nextDouble() - 0.5) * 0.3;
            double y = startY + (endY - startY) * progress + (level.random.nextDouble() - 0.5) * 0.3;
            double z = startZ + (endZ - startZ) * progress + (level.random.nextDouble() - 0.5) * 0.3;

        }
    }
}