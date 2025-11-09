package net.CoffeDino.testmod.effects;

import net.CoffeDino.testmod.particle.ModParticles;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class MourningFuneralEffect extends MobEffect {

    public MourningFuneralEffect() {
        super(MobEffectCategory.HARMFUL, 0x2F4F4F);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide() && entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            spawnContinuousParticles(entity, serverLevel, amplifier);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }


    public static void spawnContinuousParticles(LivingEntity entity, net.minecraft.server.level.ServerLevel level, int amplifier) {
        int particleCount = 5 + (amplifier * 2);
        double width = entity.getBbWidth();
        double height = entity.getBbHeight();

        for (int i = 0; i < particleCount; i++) {
            double x = entity.getX() + (level.random.nextDouble() - 0.5) * width * 1.5;
            double y = entity.getY() + level.random.nextDouble() * height;
            double z = entity.getZ() + (level.random.nextDouble() - 0.5) * width * 1.5;

            double xSpeed = (level.random.nextDouble() - 0.5) * 0.1;
            double ySpeed = level.random.nextDouble() * 0.1;
            double zSpeed = (level.random.nextDouble() - 0.5) * 0.1;

            level.sendParticles(ModParticles.MOURNING_BUTTERFLY_PARTICLES.get(),
                    x, y, z, 1, xSpeed, ySpeed, zSpeed, 0.05);
        }
    }

    public static void spawnHitParticles(LivingEntity entity, net.minecraft.server.level.ServerLevel level, int amplifier) {
        int burstCount = 8 + (amplifier * 2);

        for (int i = 0; i < burstCount; i++) {
            double x = entity.getX() + (level.random.nextDouble() - 0.5) * entity.getBbWidth();
            double y = entity.getY() + level.random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (level.random.nextDouble() - 0.5) * entity.getBbWidth();

            double xSpeed = (level.random.nextDouble() - 0.5) * 0.3;
            double ySpeed = level.random.nextDouble() * 0.3;
            double zSpeed = (level.random.nextDouble() - 0.5) * 0.3;

            level.sendParticles(ModParticles.MOURNING_BUTTERFLY_PARTICLES.get(),
                    x, y, z, 1, xSpeed, ySpeed, zSpeed, 0.1);
        }

        for (int i = 0; i < 3; i++) {
            double x = entity.getX() + (level.random.nextDouble() - 0.5) * entity.getBbWidth();
            double y = entity.getY();
            double z = entity.getZ() + (level.random.nextDouble() - 0.5) * entity.getBbWidth();

            level.sendParticles(ModParticles.MOURNING_BUTTERFLY_PARTICLES.get(),
                    x, y, z, 1, 0, 0.2, 0, 0.05);
        }
    }

    public static void spawnAdditionalParticles(LivingEntity entity, net.minecraft.server.level.ServerLevel level, int amplifier) {
        double x = entity.getX() + (level.random.nextDouble() - 0.5) * 0.5;
        double y = entity.getY() + entity.getEyeHeight() + (level.random.nextDouble() - 0.5) * 0.3;
        double z = entity.getZ() + (level.random.nextDouble() - 0.5) * 0.5;

        double xSpeed = (level.random.nextDouble() - 0.5) * 0.05;
        double ySpeed = level.random.nextDouble() * 0.02;
        double zSpeed = (level.random.nextDouble() - 0.5) * 0.05;

        level.sendParticles(ModParticles.MOURNING_BUTTERFLY_PARTICLES.get(),
                x, y, z, 1, xSpeed, ySpeed, zSpeed, 0.03);
    }
}
