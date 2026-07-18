package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.item.Custom.ShiBowItem;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import com.CoffeDino.lunacy.item.ModItems;

import javax.annotation.Nullable;

import static com.CoffeDino.lunacy.item.ModItems.SHI_ARROW;

public class ShiArrowEntity extends AbstractArrow {
    private static final double STRAIGHT_FLIGHT_DISTANCE = 500.0;
    private double distanceTraveled = 0.0;
    private Vec3 lastTickPos = null;
    public ShiArrowEntity(EntityType<? extends ShiArrowEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ShiArrowEntity(EntityType<? extends ShiArrowEntity> entityType, Level level, LivingEntity owner, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(entityType, owner, level, pickupItemStack, firedFromWeapon);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target
                && this.getOwner() instanceof LivingEntity shooter) {

            ItemStack heldBow = shooter.getMainHandItem();
            if (!(heldBow.getItem() instanceof ShiBowItem)) {
                heldBow = shooter.getOffhandItem();
            }
            if (heldBow.getItem() instanceof ShiBowItem bow) {
                bow.onArrowHit(heldBow, target, shooter);

                var enchantmentRegistry = this.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                var fireAspectHolder = enchantmentRegistry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT);
                int fireAspectLevel = heldBow.getEnchantments().getLevel(fireAspectHolder);

                if (fireAspectLevel > 0) {
                    target.igniteForSeconds(4 * fireAspectLevel);
                }
            }
        }
    }
    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(SHI_ARROW.get());
    }
    @Override
    public void tick() {
        Vec3 posBeforeTick = this.position();
        super.tick();

        if (lastTickPos != null && !this.inGround) {
            distanceTraveled += lastTickPos.distanceTo(this.position());
        }
        lastTickPos = this.position();

        if (this.level().isClientSide && !this.inGround) {
            spawnTrailParticle();
        }
    }

    private void spawnTrailParticle() {
        DustParticleOptions redDust = new DustParticleOptions(new org.joml.Vector3f(1.0F, 0.0F, 0.0F), 1.5F);
        for (int i = 0; i < 6; i++) {
            this.level().addParticle(redDust,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.1,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.1,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.1,
                    0.0, 0.0, 0.0);
        }
    }

    @Override
    protected double getDefaultGravity() {
        return distanceTraveled < STRAIGHT_FLIGHT_DISTANCE ? 0.0 : 0.05;
    }
}