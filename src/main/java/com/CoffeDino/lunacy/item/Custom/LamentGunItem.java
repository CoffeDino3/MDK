package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.entity.LamentBulletEntity;
import com.CoffeDino.lunacy.item.BulletEnhancement;
import com.CoffeDino.lunacy.item.ModItems;
import com.CoffeDino.lunacy.network.ModDataComponents;
import com.CoffeDino.lunacy.particle.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LamentGunItem extends GunItem {

    public LamentGunItem(Properties properties) {
        super(properties, 0);
    }

    @Override
    protected boolean isValidAmmo(ItemStack stack) {
        return stack.getItem() == ModItems.LAMENT_BULLET.get();
    }

    @Override
    protected void fire(Level level, Player player, boolean isAccurate) {
        if (!level.isClientSide) {
            ItemStack ammo = findAmmoStack(player);
            List<BulletEnhancement> enhancements = ammo.isEmpty()
                    ? List.of(BulletEnhancement.NONE)
                    : ammo.getOrDefault(ModDataComponents.BULLET_ENHANCEMENTS.get(), List.of(BulletEnhancement.NONE));

            LamentBulletEntity bullet = new LamentBulletEntity(level, player, isAccurate, enhancements, 5.0f);
            Vec3 look = player.getLookAngle();
            float speed = isAccurate ? 3.0f : 1.5f;
            bullet.setDeltaMovement(look.x * speed, look.y * speed, look.z * speed);
            level.addFreshEntity(bullet);
            spawnMourningButterflies(level, player);
            playShotSound(level, player);
        }
    }

    private void spawnMourningButterflies(Level level, Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 barrelPos = player.getEyePosition().add(look.x * 0.5, look.y * 0.5 - 0.2, look.z * 0.5);
        int butterflyCount = 3 + level.random.nextInt(3);
        for (int i = 0; i < butterflyCount; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;
            Vec3 spawnPos = barrelPos.add(offsetX, offsetY, offsetZ);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        spawnPos.x, spawnPos.y, spawnPos.z, 1, 0.1, 0.1, 0.1, 0.05);
                if (level.random.nextBoolean()) {
                    serverLevel.sendParticles(ModParticles.MOURNING_BUTTERFLY_PARTICLES.get(),
                            spawnPos.x, spawnPos.y, spawnPos.z, 1, 0.05, 0.05, 0.05, 0.02);
                }
            }
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF,
                    barrelPos.x, barrelPos.y, barrelPos.z, 3, 0.1, 0.1, 0.1, 0.05);
        }
    }
}