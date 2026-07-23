package com.CoffeDino.lunacy.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class MoiraiSweepEntity extends Entity {
    public static final int LIFETIME_TICKS = 12;
    private static final int BUILD_TICKS = 5;
    public static final double SWEEP_LENGTH = 7.0;
    public static final double SWEEP_WIDTH = 9.0;
    private static final double SWEEP_HEIGHT = 2.2;
    private static final float ARC_HALF_ANGLE_DEG = 70f;
    private static final float ANGLE_STEP_DEG = 6f;
    private static final double[] RADIUS_BANDS = {
            SWEEP_LENGTH * 0.45, SWEEP_LENGTH * 0.7, SWEEP_LENGTH * 1.0
    };
    private static final double[] Y_OFFSETS = {-0.25, 0.0, 0.25};
    private static final float DAMAGE_PERCENT_OF_MAX_HEALTH = 0.20f;
    private UUID ownerUUID;
    private Player cachedOwner;
    private Vec3 sweepDirection = new Vec3(0, 0, 1);
    private Vec3 rightAxis = new Vec3(1, 0, 0);
    public MoiraiSweepEntity(EntityType<? extends MoiraiSweepEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public MoiraiSweepEntity(Level level, Vec3 pos, float yaw, Player owner, Vec3 throughLine) {
        this(ModEntities.MOIRAI_SWEEP.get(), level);
        this.setPos(pos.x, pos.y, pos.z);
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.ownerUUID = owner.getUUID();
        this.cachedOwner = owner;
        this.sweepDirection = throughLine.lengthSqr() > 1.0E-4
                ? throughLine.normalize().scale(-1)
                : new Vec3(0, 0, 1);
        this.rightAxis = new Vec3(-sweepDirection.z, 0, sweepDirection.x).normalize();

        if (!level.isClientSide) {
            doDamage((ServerLevel) level);
        }
    }

    private void doDamage(ServerLevel level) {
        AABB searchBox = new AABB(this.position(), this.position())
                .inflate(Math.max(SWEEP_LENGTH, SWEEP_WIDTH), SWEEP_HEIGHT, Math.max(SWEEP_LENGTH, SWEEP_WIDTH));
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e.isAlive() && (cachedOwner == null || e != cachedOwner));
        DamageSource source = cachedOwner != null
                ? this.damageSources().indirectMagic(this, cachedOwner)
                : this.damageSources().magic();
        for (LivingEntity target : targets) {
            if (!isInsideSweepBox(target.position())) continue;

            float damage = target.getMaxHealth() * DAMAGE_PERCENT_OF_MAX_HEALTH;
            target.hurt(source, damage);

            Vec3 knock = sweepDirection.scale(0.6).add(0, 0.25, 0);
            target.setDeltaMovement(target.getDeltaMovement().add(knock));
            target.hurtMarked = true;
        }

        level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.0f, 0.5f);
    }

    private boolean isInsideSweepBox(Vec3 pos) {
        Vec3 rel = pos.subtract(this.position());
        double along = rel.dot(sweepDirection);
        double across = rel.dot(rightAxis);
        double vertical = rel.y;
        return along >= -1.0 && along <= SWEEP_LENGTH
                && Math.abs(across) <= SWEEP_WIDTH * 0.5
                && Math.abs(vertical) <= SWEEP_HEIGHT * 0.5;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (this.tickCount <= BUILD_TICKS) {
                spawnArcRing();
            }
            return;
        }
        if (this.tickCount >= LIFETIME_TICKS) {
            this.discard();
        }
    }

    private void spawnArcRing() {
        float progress = this.tickCount / (float) BUILD_TICKS; // 0 -> 1 across the build window
        float sweepFrom = -ARC_HALF_ANGLE_DEG;
        float sweepTo = -ARC_HALF_ANGLE_DEG + (2 * ARC_HALF_ANGLE_DEG * progress);

        for (float deg = sweepFrom; deg <= sweepTo; deg += ANGLE_STEP_DEG) {
            double rad = Math.toRadians(deg);
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);
            Vec3 dirAtAngle = sweepDirection.scale(cos).add(rightAxis.scale(sin));
            Vec3 tangent = sweepDirection.scale(-sin).add(rightAxis.scale(cos));
            for (double radius : RADIUS_BANDS) {
                Vec3 base = this.position().add(dirAtAngle.scale(radius));
                for (double yOff : Y_OFFSETS) {
                    this.level().addParticle(ParticleTypes.SWEEP_ATTACK,
                            base.x, base.y + 1.1 + yOff, base.z,
                            tangent.x, 0.0, tangent.z);
                }
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
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