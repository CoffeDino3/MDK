package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class HeliosSphereEntity extends Entity {

    private static final int LIFETIME_TICKS = 100;
    private static final double RADIUS = 50.0;
    private static final double VISUAL_RADIUS = 1.6;
    private static final float DAMAGE_PERCENT_OF_MAX_HEALTH = 0.05f;
    private static final int DAMAGE_INTERVAL = 10;

    private UUID ownerUUID;
    private int age;

    public HeliosSphereEntity(EntityType<? extends HeliosSphereEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public HeliosSphereEntity(Level level, Vec3 pos, LivingEntity owner) {
        this(ModEntities.HELIOS_SPHERE.get(), level);
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

        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(this.getX() - RADIUS, this.getY() - RADIUS, this.getZ() - RADIUS,
                        this.getX() + RADIUS, this.getY() + RADIUS, this.getZ() + RADIUS),
                e -> e.isAlive() && e instanceof Enemy && (ownerUUID == null || !e.getUUID().equals(ownerUUID)));

        Vec3 beamStart = this.position().add(0, 0.5, 0);

        for (LivingEntity target : targets) {
            Vec3 beamEnd = target.position().add(0, target.getEyeHeight() * 0.5, 0);
            spawnBeamParticles(beamStart, beamEnd);

            if (age % DAMAGE_INTERVAL == 0) {
                float damage = target.getMaxHealth() * DAMAGE_PERCENT_OF_MAX_HEALTH;
                target.hurt(this.damageSources().magic(), damage);
            }
        }
    }

    private void spawnBeamParticles(Vec3 start, Vec3 end) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        if (distance < 0.01) return;

        Vec3 step = direction.normalize().scale(0.2);
        int steps = (int) (distance / 0.2);

        for (int i = 0; i < steps; i++) {
            Vec3 point = start.add(step.scale(i));

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    point.x, point.y, point.z,
                    1, 0, 0, 0, 0.02);

            if (i % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        point.x, point.y, point.z,
                        1, 0.1, 0.1, 0.1, 0.01);
            }
        }

        serverLevel.sendParticles(ParticleTypes.FLASH,
                end.x, end.y, end.z,
                2, 0.2, 0.2, 0.2, 0.05);
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
            this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0, 0);
        }
        this.level().addParticle(ParticleTypes.FLASH, this.getX(), this.getY() + 0.5, this.getZ(), 0, 0, 0);
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