package com.CoffeDino.lunacy.entity.abilities;

import com.CoffeDino.lunacy.abilities.GatekeeperAbilityHandler;
import com.CoffeDino.lunacy.entity.ModEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.component.ItemAttributeModifiers.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GatekeeperPortalEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_COOLDOWN = SynchedEntityData.defineId(GatekeeperPortalEntity.class, EntityDataSerializers.INT);
    private UUID ownerUUID;
    private int shotCooldown = 0;
    private static final EntityDataAccessor<Float> DATA_YAW = SynchedEntityData.defineId(GatekeeperPortalEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PITCH = SynchedEntityData.defineId(GatekeeperPortalEntity.class, EntityDataSerializers.FLOAT);
    private float pendingHungerDrain = 0f;
    private static final int LIFESPAN_TICKS = 300;
    private int age = 0;

    public GatekeeperPortalEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public GatekeeperPortalEntity(Level level, Player owner, Vec3 pos) {
        this(ModEntities.GATEKEEPER_PORTAL.get(), level);
        this.ownerUUID = owner.getUUID();
        setPos(pos);
        Vec3 toPlayer = owner.position().add(0, 1.0, 0).subtract(pos).normalize();
        setYRot((float) Math.toDegrees(Math.atan2(-toPlayer.x, toPlayer.z)));
        setXRot((float) Math.toDegrees(Math.asin(-toPlayer.y)));
        setShotCooldown(0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_COOLDOWN, 0);
        builder.define(DATA_YAW, 0f);
        builder.define(DATA_PITCH, 0f);
    }
    public void setFacingYaw(float yaw) { entityData.set(DATA_YAW, yaw); }
    public void setFacingPitch(float pitch) { entityData.set(DATA_PITCH, pitch); }
    public float getFacingYaw() { return entityData.get(DATA_YAW); }
    public float getFacingPitch() { return entityData.get(DATA_PITCH); }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            setYRot(getYRot() + 2.0f);
            return;
        }
        age++;
        if (age >= LIFESPAN_TICKS) {
            discard();
            return;
        }

        if (shotCooldown <= 0) {
            shoot();
            shotCooldown = 20;
        } else {
            shotCooldown--;
        }
        entityData.set(DATA_COOLDOWN, shotCooldown);
    }

    private void shoot() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        Player owner = getOwner();
        if (owner == null || !owner.isAlive()) return;

        List<ItemStack> weapons = new ArrayList<>();
        for (ItemStack stack : owner.getInventory().items) {
            if (!stack.isEmpty() && isWeapon(stack)) weapons.add(stack);
        }
        if (weapons.isEmpty()) return;

        ItemStack weapon = weapons.get(owner.getRandom().nextInt(weapons.size())).copy();

        Vec3 eyePos = owner.getEyePosition();
        Vec3 lookDir = owner.getLookAngle();
        Vec3 targetPoint = eyePos.add(lookDir.scale(50.0));

        Vec3 spawnPos = position().add(0, 0.5, 0);
        Vec3 direction = targetPoint.subtract(spawnPos).normalize();
        float levelMultiplier = GatekeeperAbilityHandler.getThrownWeaponDamageMultiplier(owner);
        float finalDamage = (float) getWeaponDamage(weapon) * levelMultiplier;

        GatekeeperProjectileEntity projectile = new GatekeeperProjectileEntity(
                serverLevel, owner, weapon, finalDamage
        );
        projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        projectile.setDeltaMovement(direction.scale(1.5));
        serverLevel.addFreshEntity(projectile);

        applyShotHungerCost(owner);
    }

    private void applyShotHungerCost(Player owner) {
        pendingHungerDrain += 1.0f;
        if (pendingHungerDrain >= 1.0f) {
            pendingHungerDrain -= 1.0f;
            var food = owner.getFoodData();
            food.setFoodLevel(Math.max(0, food.getFoodLevel() - 1));
        }
    }

    private boolean isWeapon(ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers != null) {
            for (Entry entry : modifiers.modifiers()) {
                if (entry.attribute().is(Attributes.ATTACK_DAMAGE)) return true;
            }
        }
        var defaultModifiers = stack.getItem().getDefaultAttributeModifiers();
        return defaultModifiers.modifiers().stream()
                .anyMatch(e -> e.attribute().is(Attributes.ATTACK_DAMAGE));
    }

    private double getWeaponDamage(ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers != null) {
            for (Entry entry : modifiers.modifiers()) {
                if (entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                    return entry.modifier().amount();
                }
            }
        }
        for (Entry entry : stack.getItem().getDefaultAttributeModifiers().modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                return entry.modifier().amount();
            }
        }
        return 1.0;
    }

    public Player getOwner() {
        if (ownerUUID == null || level().isClientSide()) return null;
        return level().getPlayerByUUID(ownerUUID);
    }

    public void setShotCooldown(int cooldown) {
        this.shotCooldown = cooldown;
        entityData.set(DATA_COOLDOWN, cooldown);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        shotCooldown = tag.getInt("ShotCooldown");
        age = tag.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        tag.putInt("ShotCooldown", shotCooldown);
        tag.putInt("Age", age);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(0.5f, 0.5f);
    }
}
