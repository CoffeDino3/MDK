package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class DarkSphereEntity extends Entity {
    private static final int LIFETIME_TICKS = 100;
    private static final double RADIUS = 8.5;
    private static final double VISUAL_RADIUS = 1.4;
    private static final double PULL_STRENGTH = 0.15;
    private static final float DAMAGE_PERCENT_OF_MAX_HEALTH = 0.02f;
    private static final int DAMAGE_INTERVAL = 5;
    private UUID ownerUUID;
    private int age;
    public DarkSphereEntity(EntityType<? extends DarkSphereEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }
    public DarkSphereEntity(Level level, Vec3 pos, LivingEntity owner) {
        this(ModEntities.DARK_SPHERE.get(), level);
        this.setPos(pos.x, pos.y, pos.z);
        this.ownerUUID = owner.getUUID();
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            spawnClientParticles();
            return;
        }
        age++;
        if (age >= LIFETIME_TICKS) {
            this.discard();
            return;
        }
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(this.getX() - RADIUS, this.getY() - RADIUS, this.getZ() - RADIUS,
                        this.getX() + RADIUS, this.getY() + RADIUS, this.getZ() + RADIUS),
                e -> e.isAlive() && (ownerUUID == null || !e.getUUID().equals(ownerUUID)));

        for (LivingEntity target : nearby) {
            pull(target);
            if (age % DAMAGE_INTERVAL == 0) {
                float damage = target.getMaxHealth() * DAMAGE_PERCENT_OF_MAX_HEALTH;
                target.hurt(this.damageSources().magic(), damage);
            }
        }
    }

    private void pull(LivingEntity target) {
        Vec3 toCenter = this.position().subtract(target.position());
        double dist = toCenter.length();
        if (dist > 0.3) {
            Vec3 pullVec = toCenter.normalize().scale(PULL_STRENGTH);
            target.setDeltaMovement(target.getDeltaMovement().add(pullVec));
            target.hurtMarked = true;
        }
    }

    private void spawnClientParticles() {
        int particlesPerTick = 14;
        for (int i = 0; i < particlesPerTick; i++) {
            double u = this.random.nextDouble();
            double v = this.random.nextDouble();
            double theta = 2 * Math.PI * u;
            double phi = Math.acos(2 * v - 1);
            double r = VISUAL_RADIUS * Math.cbrt(this.random.nextDouble());
            double px = this.getX() + r * Math.sin(phi) * Math.cos(theta);
            double py = this.getY() + 0.5 + r * Math.cos(phi);
            double pz = this.getZ() + r * Math.sin(phi) * Math.sin(theta);
            this.level().addParticle(ParticleTypes.SQUID_INK, px, py, pz, 0, 0, 0);
        }
        this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0, 0.01, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.age = tag.getInt("Age");
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.age);
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}