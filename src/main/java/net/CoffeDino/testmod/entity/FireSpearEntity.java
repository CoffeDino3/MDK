package net.CoffeDino.testmod.entity;

import net.CoffeDino.testmod.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import static net.CoffeDino.testmod.entity.ModEntities.FIRE_SPEAR;

public class FireSpearEntity extends AbstractArrow {
    private ItemStack spearItem;
    private int lingerTime = 0;
    private static final int MAX_LINGER_TIME = 100;
    private boolean stuckInEntity = false;
    private LivingEntity stuckEntity;

    public FireSpearEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.spearItem = new ItemStack(ModItems.AGNIS_FURY.get());
    }

    public FireSpearEntity(Level level, LivingEntity shooter, ItemStack spearItem) {
        super(FIRE_SPEAR.get(), shooter, level, spearItem, null);
        this.spearItem = spearItem.copy();
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        this.setOwner(shooter);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();

        if (this.stuckInEntity || target == this.getOwner()) {
            return;
        }

        if (target instanceof LivingEntity living) {
            boolean wasCharged = false;
            if (this.spearItem != null && !this.spearItem.isEmpty()) {
                CustomData data = this.spearItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();
                wasCharged = tag.contains("Charged") && tag.getBoolean("Charged");
            }

            if (wasCharged) {
                living.igniteForSeconds(15);
            } else {
                living.igniteForSeconds(10);
            }

            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                if (wasCharged) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                            living.getX(), living.getY() + living.getEyeHeight(), living.getZ(),
                            15, 0.5, 0.5, 0.5, 0.1);
                    this.level().playSound(null, living.getX(), living.getY(), living.getZ(),
                            net.minecraft.sounds.SoundEvents.SOUL_ESCAPE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                } else {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                            living.getX(), living.getY() + living.getEyeHeight(), living.getZ(),
                            10, 0.3, 0.3, 0.3, 0.05);
                    this.level().playSound(null, living.getX(), living.getY(), living.getZ(),
                            net.minecraft.sounds.SoundEvents.FIRECHARGE_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }

            this.stuckInEntity = true;
            this.stuckEntity = living;
            this.setCritArrow(false);

            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);
            return;
        }

        super.onHitEntity(result);
    }

    @Override
    public void tick() {
        if (this.stuckInEntity && this.stuckEntity != null && this.stuckEntity.isAlive()) {
            Vec3 entityPos = this.stuckEntity.position();
            this.setPos(entityPos.x, entityPos.y + this.stuckEntity.getEyeHeight() * 0.8, entityPos.z);

            this.setDeltaMovement(this.stuckEntity.getDeltaMovement());

            if (this.level().getGameTime() % 10 == 0 && !this.level().isClientSide) {
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                        this.getX(), this.getY(), this.getZ(),
                        0, 0.1, 0);
            }

            lingerTime++;

            if (lingerTime >= MAX_LINGER_TIME && !this.level().isClientSide) {
                this.returnToPlayer();
                this.discard();
                return;
            }

            return;
        }

        super.tick();

        if (this.level().isClientSide && !this.inGround && !this.stuckInEntity) {
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY(), this.getZ(),
                    0, 0, 0);
        }

        if (this.inGround && !this.level().isClientSide) {
            lingerTime++;

            if (this.level().getGameTime() % 10 == 0) {
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                        this.getX(), this.getY(), this.getZ(),
                        0, 0.1, 0);
            }

            if (lingerTime >= MAX_LINGER_TIME) {
                this.returnToPlayer();
                this.discard();
            }
        }

        if (this.tickCount > 200 && !this.inGround && !this.stuckInEntity && !this.level().isClientSide) {
            this.returnToPlayer();
            this.discard();
        }
    }
    public boolean isStuckInEntity() {
        return this.stuckInEntity;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.stuckInEntity) {
            super.onHitBlock(result);

            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                        this.getX(), this.getY(), this.getZ(),
                        5, 0.1, 0.1, 0.1, 0.05);
            }
        }
    }

    private void returnToPlayer() {
        if (this.level().isClientSide) return;

        Entity owner = this.getOwner();
        if (owner instanceof Player player) {
            ItemStack returnStack = this.getPickupItem().copy();

            CustomData data = returnStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();

            tag.putBoolean("IsThrown", true);
            tag.putLong("ReturnTime", this.level().getGameTime());
            tag.putUUID("ThrowerUUID", player.getUUID());

            if (this.spearItem != null) {
                CustomData entityData = this.spearItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag entityTag = entityData.isEmpty() ? new CompoundTag() : entityData.copyTag();

                if (entityTag.contains("WasChargedWhenThrown") && entityTag.getBoolean("WasChargedWhenThrown")) {
                    tag.putBoolean("WasChargedWhenThrown", true);
                    if (entityTag.contains("OriginalChargeEndTime")) {
                        tag.putLong("OriginalChargeEndTime", entityTag.getLong("OriginalChargeEndTime"));
                    }
                }
            }

            returnStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            boolean added = player.getInventory().add(returnStack);
            if (!added) {
                player.drop(returnStack, false);
            }
            this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.0F);

            player.displayClientMessage(Component.literal("Spear returned!").withStyle(ChatFormatting.GREEN), true);
        }
    }

    public boolean isInGround() {
        return this.inGround || this.stuckInEntity;
    }
    public boolean isCharged() {
        if (this.spearItem != null && !this.spearItem.isEmpty()) {
            CustomData data = this.spearItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();
            return tag.contains("Charged") && tag.getBoolean("Charged");
        }
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("SpearItem")) {
            this.spearItem = ItemStack.parse(this.registryAccess(), compound.getCompound("SpearItem")).orElse(new ItemStack(ModItems.AGNIS_FURY.get()));
        }
        if (compound.contains("LingerTime")) {
            this.lingerTime = compound.getInt("LingerTime");
        }
        if (compound.contains("StuckInEntity")) {
            this.stuckInEntity = compound.getBoolean("StuckInEntity");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.spearItem != null) {
            compound.put("SpearItem", this.spearItem.save(this.registryAccess()));
        }
        compound.putInt("LingerTime", this.lingerTime);
        compound.putBoolean("StuckInEntity", this.stuckInEntity);
    }

    public ItemStack getSpearItem() {
        if (this.spearItem == null) {
            return new ItemStack(ModItems.AGNIS_FURY.get());
        }
        return this.spearItem.copy();
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        if (this.spearItem == null) {
            return new ItemStack(ModItems.AGNIS_FURY.get());
        }
        return this.spearItem.copy();
    }

    @Override
    protected ItemStack getPickupItem() {
        if (this.spearItem == null) {
            return new ItemStack(ModItems.AGNIS_FURY.get());
        }
        return this.spearItem.copy();
    }
}