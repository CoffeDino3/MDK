package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.item.BulletEnhancement;
import com.CoffeDino.lunacy.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BulletEntity extends ThrowableItemProjectile {

    private List<BulletEnhancement> enhancements = List.of(BulletEnhancement.NONE);
    private float baseDamage = 10.0f;
    private int piercedCount = 0;
    private Vec3 initialPosition;

    public BulletEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
        this.initialPosition = this.position();
        this.noCulling = true;
    }

    public BulletEntity(Level level, LivingEntity shooter, List<BulletEnhancement> enhancements, float baseDamage) {
        super(ModEntities.BULLET.get(), shooter, level);
        this.enhancements = enhancements.isEmpty() ? List.of(BulletEnhancement.NONE) : enhancements;
        this.baseDamage = baseDamage;
        this.initialPosition = this.position();
        this.setNoGravity(true);
        this.noCulling = true;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.initialPosition != null && this.position().distanceTo(this.initialPosition) > 200.0) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide) {
            spawnTrailParticles();
        }
    }

    private void spawnTrailParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = this.position();
            serverLevel.sendParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide) {
            if (enhancements.contains(BulletEnhancement.EXPLOSIVE)) {
                Vec3 pos = result.getLocation();
                this.level().explode(this, pos.x, pos.y, pos.z, BulletEnhancement.EXPLOSIVE.getExplosionPower(), Level.ExplosionInteraction.NONE);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (this.level().isClientSide || !(result.getEntity() instanceof LivingEntity target)) {
            return;
        }

        float multiplier = 1.0f;
        int maxPierce = 0;
        for (BulletEnhancement tag : enhancements) {
            multiplier += (tag.getDamageMultiplier() - 1.0f);
            maxPierce = Math.max(maxPierce, tag.getPierceCount());
        }

        float damage = baseDamage * multiplier;
        target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);

        if (this.level() instanceof ServerLevel serverLevel) {
            applyEnhancementEffects(serverLevel, target, result);
            Vec3 pos = result.getLocation();
            serverLevel.sendParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 8, 0.15, 0.15, 0.15, 0.1);
        }

        piercedCount++;
        if (piercedCount >= maxPierce) {
            this.discard();
        }
    }

    private void applyEnhancementEffects(ServerLevel serverLevel, LivingEntity target, EntityHitResult result) {
        for (BulletEnhancement tag : enhancements) {
            switch (tag) {
                case TOXIC -> target.addEffect(new MobEffectInstance(ModEffects.LEAD_POISONING, 400, 0));
                case EXPLOSIVE -> {
                    Vec3 pos = result.getLocation();
                    this.level().explode(this, pos.x, pos.y, pos.z, tag.getExplosionPower(), Level.ExplosionInteraction.NONE);
                }
                case IGNITE -> {
                    target.setRemainingFireTicks(5);
                    target.addEffect(new MobEffectInstance(ModEffects.SCORCHED, 100, 0));
                }
                case ROOTED -> {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9));
                    target.addEffect(new MobEffectInstance(ModEffects.ROOTED, 60, 0));
                }
                case SOAKED -> {
                    target.clearFire();
                    target.hurt(this.damageSources().thrown(this, this.getOwner()), 2.0f);
                    target.addEffect(new MobEffectInstance(ModEffects.SOAKED, 60, 0));
                }
                case KNOCKBACK -> {
                    Vec3 push = target.position().subtract(this.position()).normalize().scale(2.2).add(0, 0.5, 0);
                    target.setDeltaMovement(target.getDeltaMovement().add(push));
                    target.hurtMarked = true;
                    target.addEffect(new MobEffectInstance(ModEffects.FALL_VULNERABLE, 100, 0));
                }
                case RICOCHET -> ricochetTo(serverLevel, target, 2);
                case STATIC -> chainZapNearby(serverLevel, target, 1);
                case VOLATILE -> burstNearby(serverLevel, target, 2.0);
                default -> {}
            }
        }
    }

    private void ricochetTo(ServerLevel level, LivingEntity from, int count) {
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, from.getBoundingBox().inflate(6.0),
                e -> e != from && e != this.getOwner() && e.isAlive());
        nearby.sort((a, b) -> Double.compare(a.distanceToSqr(from), b.distanceToSqr(from)));
        int hits = 0;
        for (LivingEntity next : nearby) {
            if (hits >= count) break;
            next.hurt(this.damageSources().thrown(this, this.getOwner()), baseDamage);
            level.sendParticles(ParticleTypes.CRIT, next.getX(), next.getY() + next.getBbHeight() * 0.5, next.getZ(), 6, 0.2, 0.2, 0.2, 0.1);
            hits++;
        }
    }

    private void chainZapNearby(ServerLevel level, LivingEntity from, int count) {
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, from.getBoundingBox().inflate(4.0),
                e -> e != from && e != this.getOwner() && e.isAlive());
        nearby.sort((a, b) -> Double.compare(a.distanceToSqr(from), b.distanceToSqr(from)));
        int hits = 0;
        for (LivingEntity next : nearby) {
            if (hits >= count) break;
            next.hurt(this.damageSources().magic(), baseDamage * 0.5f);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, next.getX(), next.getY() + next.getBbHeight() * 0.5, next.getZ(), 6, 0.2, 0.2, 0.2, 0.1);
            hits++;
        }
    }

    private void burstNearby(ServerLevel level, LivingEntity from, double radius) {
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, from.getBoundingBox().inflate(radius),
                e -> e != from && e != this.getOwner() && e.isAlive());
        for (LivingEntity next : nearby) {
            next.hurt(this.damageSources().magic(), baseDamage * 0.4f);
        }
        level.sendParticles(ParticleTypes.END_ROD, from.getX(), from.getY() + from.getBbHeight() * 0.5, from.getZ(), 12, 0.4, 0.4, 0.4, 0.05);
    }

    public List<BulletEnhancement> getEnhancements() {
        return enhancements;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BULLET.get();
    }
}