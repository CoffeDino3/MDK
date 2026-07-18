package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.entity.ModEntities;
import com.CoffeDino.lunacy.entity.ShiArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ShiArrowItem extends ArrowItem {
    public ShiArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new ShiArrowEntity(ModEntities.SHI_ARROW.get(), level, shooter, ammo.copyWithCount(1), weapon);
    }
}