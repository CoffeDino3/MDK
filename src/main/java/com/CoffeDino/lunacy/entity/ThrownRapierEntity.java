package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class ThrownRapierEntity extends Entity {

    private static final EntityDataAccessor<ItemStack> DATA_ITEM =
            SynchedEntityData.defineId(ThrownRapierEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_STUCK =
            SynchedEntityData.defineId(ThrownRapierEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID ownerUUID;
    private UUID homingTargetUUID;
    private int homingTicksRemaining = 0;
    private boolean isHoming = false;

    private static final int MIN_HOMING_FLIGHT_TICKS = 5;
    private int homingTicksElapsed = 0;

    private boolean stuckInBlock = false;
    private int stuckTicks = 0;
    private static final int MAX_STUCK_TICKS = 1200;

    private UUID stuckMobUUID;
    private Vec3 stuckOffset = Vec3.ZERO;
    private float stuckYRot;
    private float stuckXRot;
    private boolean isFalling = false;
    private Vec3 homingAimOffset = Vec3.ZERO;

    private static final int MAX_STRAIGHT_LIFETIME = 200;
    private int lifeTicks = MAX_STRAIGHT_LIFETIME;

    public ThrownRapierEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false;
        this.entityData.set(DATA_ITEM, new ItemStack(ModItems.AMETHYST_RAPIER.get()));
    }

    public ThrownRapierEntity(Level level, Player owner, ItemStack displayStack) {
        this(ModEntities.THROWN_RAPIER.get(), level);
        this.ownerUUID = owner.getUUID();
        this.entityData.set(DATA_ITEM, displayStack.copy());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
        builder.define(DATA_STUCK, false);
    }

    public void setupHoming(LivingEntity target, int maxFlightTicks) {
        this.isHoming = true;
        this.homingTargetUUID = target.getUUID();
        this.homingTicksRemaining = maxFlightTicks;
        this.homingTicksElapsed = 0;
        this.setNoGravity(true);
        this.homingAimOffset = pickRandomAimOffset(target);
        Vec3 aimPoint = target.position().add(homingAimOffset);
        Vec3 toTarget = aimPoint.subtract(this.position());
        Vec3 initialDirection = toTarget.lengthSqr() > 1.0E-6 ? toTarget.normalize() : new Vec3(0, 0, 1);
        this.setDeltaMovement(initialDirection.scale(0.45D));
        faceMotionDirection();
    }
    private Vec3 pickRandomAimOffset(LivingEntity target) {
        double width = target.getBbWidth();
        double height = target.getBbHeight();

        double offsetX = (this.random.nextDouble() - 0.5D) * width * 0.7D;
        double offsetZ = (this.random.nextDouble() - 0.5D) * width * 0.7D;
        double offsetY = height * (0.45D + this.random.nextDouble() * 0.45D);

        return new Vec3(offsetX, offsetY, offsetZ);
    }

    public void setupStraightLine(Vec3 direction, float speed) {
        this.isHoming = false;
        this.setNoGravity(true);
        Vec3 normalized = direction.normalize();
        this.setDeltaMovement(normalized.scale(speed));
        faceMotionDirection();
    }

    public ItemStack getDisplayItem() {
        return entityData.get(DATA_ITEM);
    }

    public boolean isStuck() {
        return entityData.get(DATA_STUCK);
    }

    public Player getOwner() {
        if (ownerUUID == null) return null;
        if (!(level() instanceof ServerLevel serverLevel)) return null;
        return serverLevel.getPlayerByUUID(ownerUUID);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isStuck()) {
            handleStuckTick();
            return;
        }

        if (this.level().isClientSide) {
            return;
        }

        lifeTicks--;
        if (lifeTicks <= 0) {
            this.discard();
            return;
        }

        if (isHoming) {
            tickHoming();
        } else {
            tickStraight();
        }
    }

    private void handleStuckTick() {
        if (this.level().isClientSide) return;

        if (isFalling) {
            tickFalling();
            return;
        }

        if (stuckMobUUID != null) {
            tickStuckToMob();
            return;
        }

        stuckTicks++;
        if (stuckTicks > MAX_STUCK_TICKS) {
            this.discard();
        }
    }

    private static final float FALLING_PITCH = 100F;

    private void tickStuckToMob() {
        LivingEntity mob = getStuckMob();
        if (mob == null || !mob.isAlive()) {
            detachAndFall();
            return;
        }

        this.setPos(mob.position().add(stuckOffset));
        this.setYRot(stuckYRot);
        this.setXRot(stuckXRot);
    }

    private void tickFalling() {
        this.setNoGravity(false);

        Vec3 start = this.position();
        Vec3 motion = this.getDeltaMovement();
        Vec3 end = start.add(motion);

        BlockHitResult blockHit = this.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            this.setPos(blockHit.getLocation());
            finalizeStuckInBlock();
            return;
        }

        this.move(MoverType.SELF, motion);
        this.setDeltaMovement(motion.add(0, -0.04D, 0));
        if (motion.horizontalDistance() > 1.0E-4) {
            float yaw = (float) (Mth.atan2(motion.x, motion.z) * (180D / Math.PI));
            this.setYRot(yaw);
        }
        this.setXRot(FALLING_PITCH);
    }
    private void detachAndFall() {
        this.stuckMobUUID = null;
        this.isFalling = true;
        this.setNoGravity(false);
        this.setDeltaMovement(0, -0.02D, 0);
    }

    private void tickHoming() {
        homingTicksRemaining--;
        homingTicksElapsed++;

        LivingEntity target = getHomingTarget();
        if (target == null || !target.isAlive()) {
            beginFallingInPlace();
            return;
        }
        Vec3 aimPoint = target.position().add(homingAimOffset);
        Vec3 toTarget = aimPoint.subtract(this.position());
        double distance = toTarget.length();
        boolean closeEnough = distance < 0.6D && homingTicksElapsed >= MIN_HOMING_FLIGHT_TICKS;
        if (closeEnough || homingTicksRemaining <= 0) {
            Vec3 pullBack = toTarget.lengthSqr() > 1.0E-6 ? toTarget.normalize().scale(0.15D) : Vec3.ZERO;
            this.setPos(aimPoint.subtract(pullBack));
            resolveHitOnLivingEntity(target);
            return;
        }

        Vec3 direction = toTarget.normalize();
        Vec3 currentMotion = this.getDeltaMovement();
        double speed = Math.max(currentMotion.length(), 0.35D);
        Vec3 desiredMotion = direction.scale(speed);
        Vec3 steered = currentMotion.lengthSqr() < 1.0E-5
                ? desiredMotion
                : currentMotion.scale(0.55D).add(desiredMotion.scale(0.65D));
        double rampedSpeed = Mth.clamp(speed + 0.05D, 0.35D, 1.4D);
        this.setDeltaMovement(steered.normalize().scale(rampedSpeed));

        this.move(MoverType.SELF, this.getDeltaMovement());
        faceMotionDirection();
        HitResult blockHit = this.level().clip(new ClipContext(
                this.position().subtract(this.getDeltaMovement()),
                this.position(),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            finalizeStuckInBlock();
        }
    }

    private void tickStraight() {
        Vec3 start = this.position();
        Vec3 motion = this.getDeltaMovement();
        Vec3 end = start.add(motion);

        EntityHitResult entityHit = findEntityCollision(start, end);
        if (entityHit != null) {
            this.setPos(entityHit.getLocation());
            if (entityHit.getEntity() instanceof LivingEntity living) {
                resolveHitOnLivingEntity(living);
            } else {
                finalizeStuckInBlock();
            }
            return;
        }

        BlockHitResult blockHit = this.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            this.setPos(blockHit.getLocation());
            finalizeStuckInBlock();
            return;
        }

        this.move(MoverType.SELF, motion);
        faceMotionDirection();
    }

    private EntityHitResult findEntityCollision(Vec3 start, Vec3 end) {
        var box = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D);
        var hitEntities = this.level().getEntities(this, box,
                e -> e instanceof LivingEntity && e.isAlive() && !e.equals(getOwner()));

        Entity closestEntity = null;
        Vec3 closestPoint = null;
        double closestDistSq = Double.MAX_VALUE;

        for (Entity candidate : hitEntities) {
            var hitBox = candidate.getBoundingBox().inflate(0.3D);
            Optional<Vec3> clip = hitBox.clip(start, end);
            if (clip.isPresent()) {
                double distSq = start.distanceToSqr(clip.get());
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closestEntity = candidate;
                    closestPoint = clip.get();
                }
            }
        }

        return closestEntity != null ? new EntityHitResult(closestEntity, closestPoint) : null;
    }

    private void faceMotionDirection() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-6) {
            this.setYRot((float) (Mth.atan2(motion.x, motion.z) * (180D / Math.PI))+180);
            this.setXRot((float) (Mth.atan2(motion.y, motion.horizontalDistance()) * (180D / Math.PI))+180);
        }
    }

    private LivingEntity getHomingTarget() {
        if (homingTargetUUID == null) return null;
        if (!(this.level() instanceof ServerLevel serverLevel)) return null;

        Entity entity = serverLevel.getEntity(homingTargetUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    private LivingEntity getStuckMob() {
        if (stuckMobUUID == null) return null;
        if (!(this.level() instanceof ServerLevel serverLevel)) return null;

        Entity entity = serverLevel.getEntity(stuckMobUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    private void resolveHitOnLivingEntity(LivingEntity target) {
        Player owner = getOwner();
        ItemStack stack = this.getDisplayItem();

        if (owner != null && this.level() instanceof ServerLevel serverLevel) {
            RapierProjectileDamage.applyFullAttack(serverLevel, owner, target, stack);
        }

        if (target.isAlive()) {
            this.entityData.set(DATA_STUCK, true);
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);

            this.stuckMobUUID = target.getUUID();
            this.stuckOffset = this.position().subtract(target.position());
            this.stuckYRot = this.getYRot();
            this.stuckXRot = this.getXRot();
        } else {
            beginFallingInPlace();
        }
    }

    private void beginFallingInPlace() {
        this.entityData.set(DATA_STUCK, true);
        this.isFalling = true;
        this.stuckMobUUID = null;
        this.setNoGravity(false);
        Vec3 currentMotion = this.getDeltaMovement();
        this.setDeltaMovement(currentMotion.x * 0.3D, currentMotion.y * 0.3D, currentMotion.z * 0.3D);
    }

    private void finalizeStuckInBlock() {
        this.entityData.set(DATA_STUCK, true);
        this.setDeltaMovement(Vec3.ZERO);
        this.isFalling = false;
        this.stuckMobUUID = null;
        this.stuckInBlock = true;
        this.stuckTicks = 0;
        this.setNoGravity(true);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) ownerUUID = tag.getUUID("Owner");
        if (tag.hasUUID("HomingTarget")) homingTargetUUID = tag.getUUID("HomingTarget");
        if (tag.hasUUID("StuckMob")) stuckMobUUID = tag.getUUID("StuckMob");
        isHoming = tag.getBoolean("IsHoming");
        homingTicksRemaining = tag.getInt("HomingTicksRemaining");
        homingTicksElapsed = tag.getInt("HomingTicksElapsed");
        lifeTicks = tag.getInt("LifeTicks");
        stuckInBlock = tag.getBoolean("StuckInBlock");
        stuckTicks = tag.getInt("StuckTicks");
        isFalling = tag.getBoolean("IsFalling");
        stuckYRot = tag.getFloat("StuckYRot");
        stuckXRot = tag.getFloat("StuckXRot");
        if (tag.contains("StuckOffsetX")) {
            stuckOffset = new Vec3(tag.getDouble("StuckOffsetX"), tag.getDouble("StuckOffsetY"), tag.getDouble("StuckOffsetZ"));
        }
        if (tag.contains("HomingAimOffsetX")) {
            homingAimOffset = new Vec3(tag.getDouble("HomingAimOffsetX"), tag.getDouble("HomingAimOffsetY"), tag.getDouble("HomingAimOffsetZ"));
        }

        if (tag.contains("DisplayItem")) {
            try {
                ItemStack stack = ItemStack.parse(this.registryAccess(), tag.getCompound("DisplayItem"))
                        .orElse(new ItemStack(ModItems.AMETHYST_RAPIER.get()));
                this.entityData.set(DATA_ITEM, stack);
            } catch (Exception e) {
                this.entityData.set(DATA_ITEM, new ItemStack(ModItems.AMETHYST_RAPIER.get()));
            }
        }
        this.entityData.set(DATA_STUCK, tag.getBoolean("Stuck"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        if (homingTargetUUID != null) tag.putUUID("HomingTarget", homingTargetUUID);
        if (stuckMobUUID != null) tag.putUUID("StuckMob", stuckMobUUID);
        tag.putBoolean("IsHoming", isHoming);
        tag.putInt("HomingTicksRemaining", homingTicksRemaining);
        tag.putInt("HomingTicksElapsed", homingTicksElapsed);
        tag.putInt("LifeTicks", lifeTicks);
        tag.putBoolean("StuckInBlock", stuckInBlock);
        tag.putInt("StuckTicks", stuckTicks);
        tag.putBoolean("IsFalling", isFalling);
        tag.putFloat("StuckYRot", stuckYRot);
        tag.putFloat("StuckXRot", stuckXRot);
        tag.putDouble("StuckOffsetX", stuckOffset.x);
        tag.putDouble("StuckOffsetY", stuckOffset.y);
        tag.putDouble("StuckOffsetZ", stuckOffset.z);
        tag.putDouble("HomingAimOffsetX", homingAimOffset.x);
        tag.putDouble("HomingAimOffsetY", homingAimOffset.y);
        tag.putDouble("HomingAimOffsetZ", homingAimOffset.z);
        tag.putBoolean("Stuck", this.entityData.get(DATA_STUCK));

        ItemStack stack = this.entityData.get(DATA_ITEM);
        if (!stack.isEmpty()) {
            tag.put("DisplayItem", stack.save(this.registryAccess()));
        }
    }
}