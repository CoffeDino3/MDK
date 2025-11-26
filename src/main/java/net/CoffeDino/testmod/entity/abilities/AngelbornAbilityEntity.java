package net.CoffeDino.testmod.entity.abilities;

import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AngelbornAbilityEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_LIFETIME = SynchedEntityData.defineId(AngelbornAbilityEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HAS_TARGET =
            SynchedEntityData.defineId(AngelbornAbilityEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_TARGET_X = SynchedEntityData.defineId(AngelbornAbilityEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TARGET_Y = SynchedEntityData.defineId(AngelbornAbilityEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TARGET_Z = SynchedEntityData.defineId(AngelbornAbilityEntity.class, EntityDataSerializers.FLOAT);

    private UUID ownerUUID;
    private LivingEntity target;
    private int beamTicks = 0;
    private static final int MAX_LIFETIME = 100;
    private static final int BEAM_DURATION = 40;

    public AngelbornAbilityEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public AngelbornAbilityEntity(Level level, Player owner) {
        this(ModEntities.ANGELBORN_ABILITY.get(), level);
        this.ownerUUID = owner.getUUID();
        setLifetime(MAX_LIFETIME);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LIFETIME, 0);
        builder.define(DATA_HAS_TARGET, false);
        builder.define(DATA_TARGET_X, 0f);
        builder.define(DATA_TARGET_Y, 0f);
        builder.define(DATA_TARGET_Z, 0f);
    }

    @Override
    public void tick() {
        super.tick();

        int lifetime = getLifetime();
        if (lifetime <= 0) {
            discard();
            return;
        }
        setLifetime(lifetime - 1);
        if (target == null || !target.isAlive()) {
            findAndSetTarget();
        }
        if (target != null && target.isAlive()) {
            updateBeamRotation();
        }
        if (tickCount > 10 && target != null && target.isAlive()) {
            beamTicks++;
            if (beamTicks <= BEAM_DURATION) {
                fireBeamAtTarget();
                if (!target.isAlive()) {
                    discard();
                    return;
                }
            } else {
                discard();
            }
        } else if (tickCount > 60 || (target != null && !target.isAlive())) {
            discard();
        }
    }


    private void updateBeamRotation() {
        if (target == null || !target.isAlive()) return;
        this.entityData.set(DATA_TARGET_X, (float) target.getX());
        this.entityData.set(DATA_TARGET_Y, (float) target.getY() + target.getEyeHeight() / 2);
        this.entityData.set(DATA_TARGET_Z, (float) target.getZ());

        Vec3 start = getBeamStartPosition();
        Vec3 end = new Vec3(
                entityData.get(DATA_TARGET_X),
                entityData.get(DATA_TARGET_Y),
                entityData.get(DATA_TARGET_Z)
        );
        Vec3 diff = end.subtract(start);
        double dx = diff.x;
        double dy = diff.y;
        double dz = diff.z;

        float targetYaw = (float) (Math.toDegrees(Math.atan2(-dx, dz)));

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        float targetPitch = (float) -(Math.toDegrees(Math.atan2(dy, horizontalDist)));

        setYRot(targetYaw);
        setXRot(targetPitch);
        setYHeadRot(targetYaw);
        setYBodyRot(targetYaw);
    }




    private void findAndSetTarget() {
        Player owner = getOwner();
        if (owner == null) return;

        LivingEntity lookTarget = findLookTarget(owner);
        if (lookTarget != null) {
            setTarget(lookTarget);
            return;
        }
        List<LivingEntity> hostiles = findHostileMobsInRadius(owner, 30);
        if (!hostiles.isEmpty()) {
            setTarget(hostiles.get(level().random.nextInt(hostiles.size())));
        }
    }

    private LivingEntity findLookTarget(Player owner) {
        Vec3 start = owner.getEyePosition();
        Vec3 look = owner.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(30));

        EntityHitResult result = findEntityOnPath(owner, start, end);
        if (result != null) {
            Entity entity = result.getEntity();
            if (isValidTarget(entity, owner)) {
                return (LivingEntity) entity;
            }
        }
        return null;
    }

    private EntityHitResult findEntityOnPath(Entity owner, Vec3 start, Vec3 end) {
        BlockHitResult blockHit = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));

        EntityHitResult entityHit = null;
        double closestDistance = blockHit.getType() == HitResult.Type.MISS ?
                Double.MAX_VALUE : start.distanceTo(blockHit.getLocation());

        AABB searchBox = new AABB(start, end).inflate(1.0);
        List<Entity> entities = level().getEntities(owner, searchBox);

        for (Entity entity : entities) {
            if (isValidTarget(entity, (Player) owner)) {
                AABB entityBox = entity.getBoundingBox().inflate(0.3);
                Optional<Vec3> hitResult = entityBox.clip(start, end);
                if (hitResult.isPresent()) {
                    Vec3 hitLocation = hitResult.get();
                    double distance = start.distanceTo(hitLocation);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        entityHit = new EntityHitResult(entity, hitLocation);
                    }
                }
            }
        }

        return entityHit;
    }
    public boolean hasTarget() {
        return target != null && target.isAlive() && this.entityData.get(DATA_HAS_TARGET);
    }

    private List<LivingEntity> findHostileMobsInRadius(Player owner, double radius) {
        AABB area = new AABB(blockPosition()).inflate(radius);
        return level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> isValidTarget(entity, owner) &&
                        entity.distanceTo(owner) <= radius);
    }

    private boolean isValidTarget(Entity entity, Player owner) {
        if (!(entity instanceof LivingEntity living)) return false;
        if (entity == owner) return false;

        if (entity instanceof Player targetPlayer) {
            return owner.getHealth() < owner.getMaxHealth() * 0.5f &&
                    isPlayerLookingAt(owner, targetPlayer);
        }

        return !entity.getType().getCategory().isFriendly() &&
                entity.getType().getCategory() != MobCategory.MISC;
    }

    private boolean isPlayerLookingAt(Player player, Entity target) {
        Vec3 lookVec = player.getViewVector(1.0F).normalize();
        Vec3 toTarget = target.position().subtract(player.getEyePosition()).normalize();
        double dot = lookVec.dot(toTarget);
        return dot > 0.9;
    }

    private void setTarget(LivingEntity target) {
        this.target = target;
        this.entityData.set(DATA_HAS_TARGET, true);
        this.entityData.set(DATA_TARGET_X, (float) target.getX());
        this.entityData.set(DATA_TARGET_Y, (float) target.getY() + target.getEyeHeight() / 2);
        this.entityData.set(DATA_TARGET_Z, (float) target.getZ());
    }



    private void fireBeamAtTarget() {
        if (target == null || !target.isAlive()) return;
        this.entityData.set(DATA_TARGET_X, (float) target.getX());
        this.entityData.set(DATA_TARGET_Y, (float) target.getY() + target.getEyeHeight() / 2);
        this.entityData.set(DATA_TARGET_Z, (float) target.getZ());

        Vec3 start = getBeamStartPosition();
        Vec3 end = new Vec3(
                entityData.get(DATA_TARGET_X),
                entityData.get(DATA_TARGET_Y),
                entityData.get(DATA_TARGET_Z)
        );

        spawnBeamParticles(start, end);

        if (beamTicks % 10 == 0 && !level().isClientSide()) {
            boolean wasKilled = target.getHealth() <= 3.0f;
            target.hurt(level().damageSources().indirectMagic(this, getOwner()), 3.0f);

            if (!target.isAlive() || wasKilled) {
                discard();
                return;
            }
        }
    }



    private void spawnBeamParticles(Vec3 start, Vec3 end) {
        if (level().isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) level();
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
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
                5, 0.3, 0.3, 0.3, 0.1);
    }

    public Player getOwner() {
        if (ownerUUID == null || level().isClientSide()) return null;
        return level().getPlayerByUUID(ownerUUID);
    }

    public int getLifetime() {
        return entityData.get(DATA_LIFETIME);
    }

    public void setLifetime(int lifetime) {
        entityData.set(DATA_LIFETIME, lifetime);
    }
    public Vec3 getBeamDirection() {
        if (!hasTarget()) return null;

        Vec3 start = getBeamStartPosition();
        Vec3 end = new Vec3(
                entityData.get(DATA_TARGET_X),
                entityData.get(DATA_TARGET_Y),
                entityData.get(DATA_TARGET_Z)
        );

        return end.subtract(start).normalize();
    }

    public Vec3 getBeamStartPosition() {
        return position().add(0, 0.5, 0);
    }


    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(0.1f, 0.1f);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        refreshDimensions();
    }






    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("Owner")) {
            ownerUUID = compound.getUUID("Owner");
        }
        setLifetime(compound.getInt("Lifetime"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (ownerUUID != null) {
            compound.putUUID("Owner", ownerUUID);
        }
        compound.putInt("Lifetime", getLifetime());
    }
}