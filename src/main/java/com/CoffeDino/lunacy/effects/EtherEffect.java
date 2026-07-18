package com.CoffeDino.lunacy.effects;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = Lunacy.MODID)
public class EtherEffect extends MobEffect {
    public EtherEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x87CEEB);
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void handleLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel serverLevel)) return;
        if (!target.hasEffect(ModEffects.ETHER)) return;

        DamageSource source = event.getSource();
        LivingEntity attacker = getAttacker(source);
        if (attacker == null || attacker == target || !attacker.isAlive()) return;
        if (!(source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MAGIC))) {
            return;
        }

        float damage = event.getNewDamage();
        MobEffectInstance effectInstance = target.getEffect(ModEffects.ETHER);
        int amplifier = effectInstance != null ? effectInstance.getAmplifier() : 0;
        float reflectionPercent = 0.35f + (amplifier * 0.15f);
        float reflectedDamage = damage * reflectionPercent;

        if (reflectedDamage <= 0) return;
        attacker.hurt(target.damageSources().magic(), reflectedDamage);
        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                6, 0.4, 0.4, 0.4, 0.1
        );

        serverLevel.sendParticles(
                ParticleTypes.CRIT,
                attacker.getX(), attacker.getY() + attacker.getBbHeight() * 0.5, attacker.getZ(),
                3, 0.3, 0.3, 0.3, 0.05
        );
        target.level().playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.PLAYERS,
                0.5F,
                1.0F
        );
    }

    private static LivingEntity getAttacker(DamageSource source) {
        if (source.getDirectEntity() instanceof LivingEntity living) return living;
        if (source.getEntity() instanceof LivingEntity living) return living;
        return null;
    }
}