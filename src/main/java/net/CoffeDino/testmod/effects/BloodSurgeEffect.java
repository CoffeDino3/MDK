package net.CoffeDino.testmod.effects;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BloodSurgeEffect extends MobEffect {
    public BloodSurgeEffect() {
        super(
                MobEffectCategory.BENEFICIAL,
                0x8B0000
        );
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void handleLivingDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();

        if (source.getEntity() instanceof LivingEntity attacker &&
                attacker.hasEffect(ModEffects.BLOOD_SURGE.getHolder().get()) &&
                !attacker.level().isClientSide() &&
                target != attacker) {

            float damage = event.getAmount();
            MobEffectInstance effectInstance = attacker.getEffect(ModEffects.BLOOD_SURGE.getHolder().get());
            int amplifier = effectInstance != null ? effectInstance.getAmplifier() : 0;
            float healPercent = 0.20f + (amplifier * 0.05f);
            float healAmount = damage * healPercent;

            if (healAmount > 0) {
                attacker.heal(healAmount);
                if (attacker.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            5, 0.3, 0.3, 0.3, 0.1
                    );
                    if (attacker instanceof Player) {
                        serverLevel.sendParticles(
                                net.minecraft.core.particles.ParticleTypes.HEART,
                                attacker.getX(), attacker.getY() + 1.5, attacker.getZ(),
                                1, 0.2, 0.2, 0.2, 0.05
                        );
                    } else {
                        serverLevel.sendParticles(
                                net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                                attacker.getX(), attacker.getY() + attacker.getBbHeight() * 0.5, attacker.getZ(),
                                3, 0.3, 0.3, 0.3, 0.05
                        );
                    }
                    spawnBloodFlowParticles(target, attacker, serverLevel);
                }
                attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                        net.minecraft.sounds.SoundEvents.PLAYER_HURT,
                        net.minecraft.sounds.SoundSource.NEUTRAL, 0.7F, 0.8F);
            }
        }
    }

    private static void spawnBloodFlowParticles(LivingEntity from, LivingEntity to, net.minecraft.server.level.ServerLevel level) {
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

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.DRIPPING_HONEY,
                    x, y, z, 1, 0, 0, 0, 0.02
            );
        }
    }
}