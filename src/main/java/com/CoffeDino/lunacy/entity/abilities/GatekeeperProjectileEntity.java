package com.CoffeDino.lunacy.entity.abilities;

import com.CoffeDino.lunacy.entity.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class GatekeeperProjectileEntity extends Projectile {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(GatekeeperProjectileEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(GatekeeperProjectileEntity.class, EntityDataSerializers.FLOAT);
    private UUID ownerUUID;

    public GatekeeperProjectileEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public GatekeeperProjectileEntity(ServerLevel level, Player owner, ItemStack item, float damage) {
        this(ModEntities.GATEKEEPER_PROJECTILE.get(), level);
        this.ownerUUID = owner.getUUID();
        setItem(item);
        setDamage(damage);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
        builder.define(DATA_DAMAGE, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            Vec3 motion = getDeltaMovement();
            Vec3 from = position();
            Vec3 to = from.add(motion);
            HitResult result = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (result.getType() != HitResult.Type.MISS) {
                onHit(result);
                return;
            }

            setPos(to.x, to.y, to.z);

            if (tickCount > 100) discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide) {
            Entity target = result.getEntity();
            if (target instanceof LivingEntity living && !living.is(getOwner())) {
                living.hurt(damageSources().mobAttack(getOwner() instanceof LivingEntity l ? l : null), getDamage());
            }
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!level().isClientSide) {
            discard();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(1.7f, 1.7f);
    }

    public Player getOwner() {
        if (ownerUUID == null || level().isClientSide()) return null;
        return level().getPlayerByUUID(ownerUUID);
    }

    public void setItem(ItemStack stack) {
        entityData.set(DATA_ITEM, stack.copy());
    }

    public ItemStack getItem() {
        return entityData.get(DATA_ITEM);
    }

    public void setDamage(float damage) {
        entityData.set(DATA_DAMAGE, damage);
    }

    public float getDamage() {
        return entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        if (tag.contains("Item")) {
            setItem(ItemStack.parseOptional(level().registryAccess(), tag.getCompound("Item")));
        }
        setDamage(tag.getFloat("Damage"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        tag.put("Item", getItem().save(level().registryAccess()));
        tag.putFloat("Damage", getDamage());
    }
}