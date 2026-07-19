package com.CoffeDino.lunacy.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class RocaBoulderEntity extends ThrowableProjectile {

    private static final EntityDataAccessor<BlockState> BLOCK_STATE =
            SynchedEntityData.defineId(RocaBoulderEntity.class, EntityDataSerializers.BLOCK_STATE);

    private static final int MAX_LIFE_TICKS = 100;
    private static final int LAUNCH_DELAY_TICKS = 10;
    private static final float LAUNCH_UP_SPEED = 0.7F;

    private float damage;
    private int life;
    private boolean redirected = false;
    private net.minecraft.world.entity.LivingEntity target;
    private float travelSpeed;

    public RocaBoulderEntity(EntityType<? extends RocaBoulderEntity> type, Level level) {
        super(type, level);
    }

    public RocaBoulderEntity(ServerLevel level, Player owner, Vec3 spawnPos,
                             net.minecraft.world.entity.LivingEntity target,
                             BlockState state, float damage, float speed) {
        this(com.CoffeDino.lunacy.entity.ModEntities.ROCA_BOULDER.get(), level);
        this.setOwner(owner);

        this.setPos(spawnPos.x, spawnPos.y + 0.3, spawnPos.z);

        this.entityData.set(BLOCK_STATE, state);
        this.damage = damage;
        this.target = target;
        this.travelSpeed = speed;

        this.shoot(0, LAUNCH_UP_SPEED, 0, 1.0F, 0.0F);
    }

    public BlockState getBlockState() {
        return this.entityData.get(BLOCK_STATE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BLOCK_STATE, Blocks.STONE.defaultBlockState());
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            life++;

            if (!redirected && life >= LAUNCH_DELAY_TICKS) {
                redirectAtTarget();
            }

            if (life > MAX_LIFE_TICKS) {
                this.discard();
            }
        }
    }

    private void redirectAtTarget() {
        redirected = true;

        if (target == null || !target.isAlive()) {
            return;
        }

        Vec3 from = this.position();
        Vec3 to = target.getBoundingBox().getCenter();
        Vec3 dir = to.subtract(from).normalize();

        this.setDeltaMovement(dir.scale(travelSpeed));
        this.hasImpulse = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;

        Entity hit = result.getEntity();
        Entity owner = this.getOwner();

        if (hit != owner) {
            DamageSource source = this.level().damageSources().thrown(this, owner);
            hit.hurt(source, this.damage);
        }

        spawnBreakParticles();
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            spawnBreakParticles();
            this.discard();
        }
    }

    private void spawnBreakParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, getBlockState()),
                    this.getX(), this.getY(), this.getZ(), 12, 0.2, 0.2, 0.2, 0.1);
        }
    }
}