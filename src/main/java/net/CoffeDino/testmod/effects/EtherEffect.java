package net.CoffeDino.testmod.effects;

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
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EtherEffect extends MobEffect {
    public EtherEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x87CEEB); // Light blue
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void handleLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel serverLevel)) return;
        if (!target.hasEffect(ModEffects.ETHER.getHolder().get())) return;

        DamageSource source = event.getSource();
        LivingEntity attacker = getAttacker(source);
        if (attacker == null || attacker == target || !attacker.isAlive()) return;
        if (!(source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MAGIC))) {
            return;
        }

        float damage = event.getAmount();
        MobEffectInstance effectInstance = target.getEffect(ModEffects.ETHER.getHolder().get());
        int amplifier = effectInstance != null ? effectInstance.getAmplifier() : 0;
        float reflectionPercent = 0.10f + (amplifier * 0.05f);
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
