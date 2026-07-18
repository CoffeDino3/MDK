package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BorontAvatarEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_SWING_TICKS = SynchedEntityData.defineId(BorontAvatarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(BorontAvatarEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public static final float DAMAGE_MULTIPLIER = 1.75f;
    public static final int SUMMON_COOLDOWN_TICKS = 600;
    private static final int ROTATION_DELAY_TICKS = 20;
    private static final int ATTACK_DELAY_TICKS = 15;
    private static final int MAX_LIFE_TICKS = 600;
    private static final int SWING_ANIM_TICKS = 16;
    private static final int HIT_CONNECT_TICKS = 10;
    private static final int STRIKE_RANGE = 13;
    private static final double STRIKE_RADIUS = 5.0D;
    private static final double SWEEP_DOT_THRESHOLD = -0.6D;
    private static final double SWEEP_KNOCKBACK_STRENGTH = 0.45D;

    private UUID ownerUUID;
    private int age = 0;
    private Item summonItem;

    private final Deque<float[]> rotationHistory = new ArrayDeque<>();
    private final Deque<ScheduledStrike> scheduledStrikes = new ArrayDeque<>();
    private final Deque<PendingHit> pendingHits = new ArrayDeque<>();

    private record ScheduledStrike(int ticksRemaining, float damage) {
        ScheduledStrike decremented() {
            return new ScheduledStrike(ticksRemaining - 1, damage);
        }
    }

    private record PendingHit(int ticksRemaining, float damage) {
        PendingHit decremented() {
            return new PendingHit(ticksRemaining - 1, damage);
        }
    }

    public BorontAvatarEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public BorontAvatarEntity(Level level, Player owner) {
        this(ModEntities.BORONT_AVATAR.get(), level);
        this.ownerUUID = owner.getUUID();
        this.entityData.set(DATA_OWNER_UUID, Optional.of(this.ownerUUID));
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        followOwner(owner);
    }

    public void setSummonItem(Item item) {
        this.summonItem = item;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SWING_TICKS, 0);
        builder.define(DATA_OWNER_UUID, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        age++;
        if (age >= MAX_LIFE_TICKS) {
            applyCooldownToOwner();
            this.discard();
            return;
        }

        Player owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        followOwner(owner);
        recordAndApplyDelayedRotation(owner);
        tickScheduledStrikes();
        tickSwingCountdown();
    }

    private void followOwner(Player owner) {
        Vec3 back = Vec3.directionFromRotation(0, owner.getYRot() + 180).scale(1.2D);
        Vec3 pos = owner.position().add(back).add(0, 1.0D, 0);
        this.setPos(pos.x, pos.y, pos.z);
    }

    private void recordAndApplyDelayedRotation(Player owner) {
        rotationHistory.addLast(new float[]{owner.getYRot(), owner.getXRot()});
        if (rotationHistory.size() > ROTATION_DELAY_TICKS) {
            float[] delayed = rotationHistory.removeFirst();
            this.setYRot(delayed[0]);
            this.setXRot(delayed[1]);
        }
    }

    private void tickSwingCountdown() {
        int ticksLeft = this.entityData.get(DATA_SWING_TICKS);
        if (ticksLeft > 0) {
            this.entityData.set(DATA_SWING_TICKS, ticksLeft - 1);
        }
    }

    private void tickScheduledStrikes() {
        if (!scheduledStrikes.isEmpty()) {
            Deque<ScheduledStrike> next = new ArrayDeque<>();
            for (ScheduledStrike strike : scheduledStrikes) {
                if (strike.ticksRemaining() <= 0) {
                    triggerSwing(strike.damage());
                } else {
                    next.addLast(strike.decremented());
                }
            }
            scheduledStrikes.clear();
            scheduledStrikes.addAll(next);
        }

        if (!pendingHits.isEmpty()) {
            Deque<PendingHit> next = new ArrayDeque<>();
            for (PendingHit hit : pendingHits) {
                if (hit.ticksRemaining() <= 0) {
                    applyHit(hit.damage());
                } else {
                    next.addLast(hit.decremented());
                }
            }
            pendingHits.clear();
            pendingHits.addAll(next);
        }
    }

    private void triggerSwing(float damage) {
        this.entityData.set(DATA_SWING_TICKS, SWING_ANIM_TICKS);
        pendingHits.addLast(new PendingHit(HIT_CONNECT_TICKS, damage));
    }

    private void applyHit(float damage) {
        Player owner = getOwner();
        if (owner == null || !(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 look = Vec3.directionFromRotation(owner.getXRot(), owner.getYRot());
        Vec3 origin = owner.position().add(0, owner.getBbHeight() * 0.5D, 0);
        Vec3 reachPoint = origin.add(look.scale(STRIKE_RANGE));
        AABB sweepBox = new AABB(origin, reachPoint).inflate(STRIKE_RADIUS);

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, sweepBox,
                e -> e.isAlive() && !e.equals(owner));

        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().subtract(origin);
            if (toTarget.lengthSqr() < 1.0E-6) continue;
            Vec3 toTargetNorm = toTarget.normalize();
            if (toTargetNorm.dot(look) < SWEEP_DOT_THRESHOLD) continue;

            target.hurt(owner.damageSources().playerAttack(owner), damage);
            target.knockback(SWEEP_KNOCKBACK_STRENGTH, -toTargetNorm.x, -toTargetNorm.z);
        }

        spawnSweepParticles(serverLevel, origin, look);
    }

    private void spawnSweepParticles(ServerLevel level, Vec3 origin, Vec3 look) {
        Vec3 groundPoint = origin.add(look.scale(STRIKE_RANGE * 0.6D));
        double groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING,
                (int) groundPoint.x, (int) groundPoint.z);

        level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                groundPoint.x, groundY + 0.1D, groundPoint.z,
                1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.CRIT,
                groundPoint.x, groundY + 0.3D, groundPoint.z,
                16, STRIKE_RADIUS * 0.6, 0.15, STRIKE_RADIUS * 0.6, 0.05);
    }

    public void scheduleStrike(float damage) {
        scheduledStrikes.addLast(new ScheduledStrike(ATTACK_DELAY_TICKS, damage));
    }

    private void applyCooldownToOwner() {
        if (summonItem == null) return;
        Player owner = getOwner();
        if (owner == null) return;
        owner.getCooldowns().addCooldown(summonItem, SUMMON_COOLDOWN_TICKS);
    }

    public Player getOwner() {
        if (ownerUUID == null || !(this.level() instanceof ServerLevel serverLevel)) return null;
        return serverLevel.getPlayerByUUID(ownerUUID);
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public int getSwingTicksLeft() {
        return this.entityData.get(DATA_SWING_TICKS);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public static BorontAvatarEntity findAvatarFor(ServerLevel level, Player owner) {
        AABB area = owner.getBoundingBox().inflate(32.0D);
        List<BorontAvatarEntity> found = level.getEntitiesOfClass(BorontAvatarEntity.class, area,
                e -> owner.getUUID().equals(e.ownerUUID));
        return found.isEmpty() ? null : found.get(0);
    }

    public static void queueEcho(ServerLevel level, Player owner, float damage) {
        BorontAvatarEntity avatar = findAvatarFor(level, owner);
        if (avatar != null) {
            avatar.scheduleStrike(damage);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
            this.entityData.set(DATA_OWNER_UUID, Optional.of(ownerUUID));
        }
        age = tag.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        tag.putInt("Age", age);
    }
}