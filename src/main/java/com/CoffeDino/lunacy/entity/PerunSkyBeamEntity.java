package com.CoffeDino.lunacy.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PerunSkyBeamEntity extends Entity {

    public static final int FADE_IN_TICKS = 3;
    public static final int HOLD_TICKS = 4;
    public static final int FADE_OUT_TICKS = 5;
    public static final int LIFETIME_TICKS = FADE_IN_TICKS + HOLD_TICKS + FADE_OUT_TICKS;
    public static final float BEAM_HEIGHT = 128.0f;
    private static final EntityDataAccessor<Integer> DATA_AGE =
            SynchedEntityData.defineId(PerunSkyBeamEntity.class, EntityDataSerializers.INT);

    public PerunSkyBeamEntity(EntityType<? extends PerunSkyBeamEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_AGE, 0);
    }

    public int getAge() {
        return this.entityData.get(DATA_AGE);
    }
    public float getVisibility(float partialTick) {
        float age = getAge() + partialTick;
        if (age < FADE_IN_TICKS) {
            return Mth.clamp(age / FADE_IN_TICKS, 0f, 1f);
        }
        if (age < FADE_IN_TICKS + HOLD_TICKS) {
            return 1.0f;
        }
        float fadeAge = age - (FADE_IN_TICKS + HOLD_TICKS);
        return 1.0f - Mth.clamp(fadeAge / FADE_OUT_TICKS, 0f, 1f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            int age = getAge();
            if (age >= LIFETIME_TICKS) {
                this.discard();
                return;
            }
            this.entityData.set(DATA_AGE, age + 1);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_AGE, tag.getInt("Age"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
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