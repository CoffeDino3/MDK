package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.particle.PerunFlashParticleOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public class PerunOrbitalStrikeEntity extends Entity {

    public static final int DESCEND_TICKS = 16;
    public static final int IMPACT_TICK = DESCEND_TICKS;
    public static final int LINGER_TICKS = 20;
    public static final int LIFETIME_TICKS = IMPACT_TICK + LINGER_TICKS;
    public static final double STRIKE_RADIUS = 8.0;
    public static final float STRIKE_DAMAGE_PERCENT = 0.4f;
    public static final double KNOCKBACK = 0.9;
    private static final float MAX_RING_RADIUS = (float) (STRIKE_RADIUS * 1.8);
    private static final float[] RING_DELAY_FRACTIONS = {0f, 0.15f, 0.35f};
    private static final float[] RING_MAX_RADIUS_FRACTIONS = {0.4f, 0.7f, 1.0f};
    private static final int PARTICLE_TICK_INTERVAL = 2;
    private static final float CENTER_FLASH_MIN_SCALE = 1.5f;
    private static final float CENTER_FLASH_MAX_SCALE = MAX_RING_RADIUS;
    private static final double DESCEND_HITBOX_RADIUS = 1.0;

    private static final EntityDataAccessor<Integer> DATA_AGE =
            SynchedEntityData.defineId(PerunOrbitalStrikeEntity.class, EntityDataSerializers.INT);

    private UUID ownerUUID;
    private Player cachedOwner;

    public PerunOrbitalStrikeEntity(EntityType<? extends PerunOrbitalStrikeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void setOwner(Player owner) {
        this.ownerUUID = owner.getUUID();
        this.cachedOwner = owner;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_AGE, 0);
    }

    public int getAge() {
        return this.entityData.get(DATA_AGE);
    }

    public float getDescendProgress(float partialTick) {
        return Mth.clamp((getAge() + partialTick) / (float) DESCEND_TICKS, 0f, 1f);
    }

    private double currentEffectRadius() {
        int age = getAge();
        if (age < IMPACT_TICK) {
            return DESCEND_HITBOX_RADIUS;
        }
        float lingerT = age - IMPACT_TICK;
        float overallProgress = Mth.clamp(lingerT / LINGER_TICKS, 0f, 1f);
        return CENTER_FLASH_MIN_SCALE + (CENTER_FLASH_MAX_SCALE - CENTER_FLASH_MIN_SCALE) * easeOutCubic(overallProgress);
    }
    private void updateHitboxSize() {
        double radius = currentEffectRadius();
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        this.setBoundingBox(new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        ));
    }
    @Override
    public void tick() {
        super.tick();
        int age = getAge();
        updateHitboxSize();
        if (!this.level().isClientSide) {
            if (age == IMPACT_TICK) {
                doImpact((ServerLevel) this.level());
            }
            if (age > IMPACT_TICK) {
                spawnLingeringParticles((ServerLevel) this.level(), age);
            }
            if (age >= LIFETIME_TICKS) {
                this.discard();
                return;
            }
            this.entityData.set(DATA_AGE, age + 1);
        }
    }

    private void doImpact(ServerLevel level) {
        level.playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.PLAYERS, 2.0f, 1.1f);

        AABB area = new AABB(this.position(), this.position()).inflate(STRIKE_RADIUS);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && (cachedOwner == null || e != cachedOwner));

        DamageSource source = cachedOwner != null
                ? this.damageSources().indirectMagic(this, cachedOwner)
                : this.damageSources().magic();

        for (LivingEntity target : targets) {
            float damage = target.getMaxHealth() * STRIKE_DAMAGE_PERCENT;
            target.hurt(source, damage);
            Vec3 push = target.position().subtract(this.position());
            double dist = Math.max(push.length(), 0.1);
            Vec3 knock = push.scale(1.0 / dist).scale(KNOCKBACK).add(0, 0.4, 0);
            target.setDeltaMovement(target.getDeltaMovement().add(knock));
            target.hurtMarked = true;
        }
    }
    private void spawnLingeringParticles(ServerLevel level, int age) {
        float lingerT = age - IMPACT_TICK;
        if (((int) lingerT) % PARTICLE_TICK_INTERVAL != 0) return;

        float overallProgress = Mth.clamp(lingerT / LINGER_TICKS, 0f, 1f);
        spawnCenterFlash(level, overallProgress);

        for (int i = 0; i < RING_DELAY_FRACTIONS.length; i++) {
            float delayFraction = RING_DELAY_FRACTIONS[i];
            float delayTicks = delayFraction * LINGER_TICKS;
            float local = lingerT - delayTicks;
            if (local < 0f) continue;

            float span = LINGER_TICKS * (1f - delayFraction);
            float localProgress = Mth.clamp(local / span, 0f, 1f);
            if (localProgress >= 1f) continue;

            float waveMaxRadius = MAX_RING_RADIUS * RING_MAX_RADIUS_FRACTIONS[i];
            float radius = waveMaxRadius * easeOutCubic(localProgress);
            spawnParticleRing(level, radius);
        }
    }

    private void spawnParticleRing(ServerLevel level, float radius) {
        int count = Math.max(8, (int) (radius * 4f));
        double y = this.getY() + 0.1;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0, 0, 0, 0.0);
        }
    }

    private void spawnCenterFlash(ServerLevel level, float overallProgress) {
        float scale = CENTER_FLASH_MIN_SCALE + (CENTER_FLASH_MAX_SCALE - CENTER_FLASH_MIN_SCALE) * easeOutCubic(overallProgress);
        int totalParticles = (int) (50 + (150 * overallProgress));
        for (int i = 0; i < totalParticles; i++) {
            double theta = 2 * Math.PI * level.random.nextDouble();
            double phi = Math.acos(2 * level.random.nextDouble() - 1);
            double radius = scale * Math.cbrt(level.random.nextDouble());
            double x = this.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double y = this.getY() + 0.1 + radius * Math.cos(phi);
            double z = this.getZ() + radius * Math.sin(phi) * Math.sin(theta);
            level.sendParticles(ParticleTypes.FLASH,
                    x, y, z,
                    1, 0, 0, 0, 0.0);
        }
        for (int i = 0; i < 20; i++) {
            double theta = 2 * Math.PI * level.random.nextDouble();
            double phi = Math.acos(2 * level.random.nextDouble() - 1);
            double radius = scale * 0.2 * Math.cbrt(level.random.nextDouble());

            double x = this.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double y = this.getY() + 0.1 + radius * Math.cos(phi);
            double z = this.getZ() + radius * Math.sin(phi) * Math.sin(theta);
            level.sendParticles(ParticleTypes.FLASH,
                    x, y, z,
                    1, 0, 0, 0, 0.0);
        }
    }

    private static float easeOutCubic(float x) {
        float f = 1f - x;
        return 1f - f * f * f;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        this.entityData.set(DATA_AGE, tag.getInt("Age"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        tag.putInt("Age", getAge());
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }
}