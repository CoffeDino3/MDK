package com.CoffeDino.lunacy.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MoiraiPortalEntity extends Entity {

    public static final int CAST_PORTAL_LIFETIME = 14;
    public static final int EXIT_PORTAL_LIFETIME = 20;
    private static final int FADE_IN_TICKS = 4;
    private static final int FADE_OUT_TICKS = 6;
    private static final EntityDataAccessor<Integer> DATA_LIFETIME =
            SynchedEntityData.defineId(MoiraiPortalEntity.class, EntityDataSerializers.INT);

    public MoiraiPortalEntity(EntityType<? extends MoiraiPortalEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public MoiraiPortalEntity(Level level, Vec3 pos, float yaw, int lifetime) {
        this(ModEntities.MOIRAI_PORTAL.get(), level);
        this.setPos(pos.x, pos.y, pos.z);
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.entityData.set(DATA_LIFETIME, lifetime);
    }

    public int getLifetime() {
        return this.entityData.get(DATA_LIFETIME);
    }
    public float getVisibility(float partialTick) {
        float age = this.tickCount + partialTick;
        int lifetime = getLifetime();

        if (age < FADE_IN_TICKS) {
            return Mth.clamp(age / FADE_IN_TICKS, 0f, 1f);
        }
        float fadeStart = lifetime - FADE_OUT_TICKS;
        if (age > fadeStart) {
            return Mth.clamp(1f - (age - fadeStart) / FADE_OUT_TICKS, 0f, 1f);
        }
        return 1.0f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LIFETIME, CAST_PORTAL_LIFETIME);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount >= getLifetime()) {
            this.discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_LIFETIME, tag.getInt("Lifetime"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Lifetime", getLifetime());
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }
}