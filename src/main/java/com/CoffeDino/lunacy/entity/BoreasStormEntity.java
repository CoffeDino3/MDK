package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class BoreasStormEntity extends Entity {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(BoreasStormEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final int LIFETIME_TICKS = 160;
    public static final double RADIUS = 25.0;
    public static final double HEIGHT_RANGE = 12.0;
    private static final int DAMAGE_INTERVAL = 10;
    private static final float DAMAGE_PERCENT_OF_MAX_HEALTH = 0.03f;
    private static final int SLOW_REFRESH_TICKS = 30;
    private static final int TICKS_PER_SLOW_LEVEL = 30;
    private static final int MAX_SLOW_AMPLIFIER = 5;
    private static final int BLOCK_BREAK_INTERVAL = 30;
    private static final int CORE_RADIUS = 1;
    private static final int MAX_RADIUS = 2;
    public static final float GUST_DROP_CHANCE_CLIENT = 0.15f;
    private int age;
    private final Map<UUID, Integer> exposureTicks = new HashMap<>();
    public BoreasStormEntity(EntityType<? extends BoreasStormEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }
    public BoreasStormEntity(Level level, Vec3 pos, LivingEntity owner) {
        this(ModEntities.BOREAS_STORM.get(), level);
        this.setPos(pos.x, pos.y, pos.z);
        this.entityData.set(DATA_OWNER_UUID, Optional.of(owner.getUUID()));
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER_UUID, Optional.empty());
    }
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        age++;
        if (age >= LIFETIME_TICKS) {
            this.discard();
            return;
        }
        List<LivingEntity> targets = getAffectedMobs();
        Set<UUID> currentIds = new HashSet<>();
        for (LivingEntity target : targets) {
            currentIds.add(target.getUUID());

            int ticksExposed = exposureTicks.merge(target.getUUID(), 1, Integer::sum);
            int amplifier = Math.min(MAX_SLOW_AMPLIFIER, ticksExposed / TICKS_PER_SLOW_LEVEL);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_REFRESH_TICKS, amplifier, false, false));
            if (age % DAMAGE_INTERVAL == 0) {
                float damage = target.getMaxHealth() * DAMAGE_PERCENT_OF_MAX_HEALTH;
                target.hurt(this.damageSources().magic(), damage);
            }
            if (age % BLOCK_BREAK_INTERVAL == 0) {
                breakGroundUnder(target);
            }
        }
        exposureTicks.keySet().removeIf(id -> !currentIds.contains(id));
    }
    private void breakGroundUnder(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        BlockPos feet = target.blockPosition().below();
        for (int x = -MAX_RADIUS; x <= MAX_RADIUS; x++) {
            for (int z = -MAX_RADIUS; z <= MAX_RADIUS; z++) {
                int ringDistance = Math.max(Math.abs(x), Math.abs(z));
                if (ringDistance > CORE_RADIUS) {
                    float chance = 0.5f / ringDistance;
                    if (this.random.nextFloat() > chance) continue;
                }
                BlockPos pos = feet.offset(x, 0, z);
                BlockState state = serverLevel.getBlockState(pos);
                if (state.isAir()) continue;
                if (state.getDestroySpeed(serverLevel, pos) < 0) continue;
                serverLevel.destroyBlock(pos, false, this);
            }
        }
    }
    public List<LivingEntity> getAffectedMobs() {
        AABB box = new AABB(this.getX() - RADIUS, this.getY() - HEIGHT_RANGE, this.getZ() - RADIUS,
                this.getX() + RADIUS, this.getY() + HEIGHT_RANGE, this.getZ() + RADIUS);

        UUID owner = getOwnerUUID();
        return this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && !Objects.equals(e.getUUID(), owner));
    }
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.age = tag.getInt("Age");
        if (tag.hasUUID("Owner")) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(tag.getUUID("Owner")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.age);
        this.entityData.get(DATA_OWNER_UUID).ifPresent(id -> tag.putUUID("Owner", id));
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