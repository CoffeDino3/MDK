package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.item.Custom.AmethystRapierItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
public final class RapierProjectileDamage {

    private RapierProjectileDamage() {
    }

    public static void applyFullAttack(ServerLevel level, Player owner, LivingEntity target, ItemStack rapierStack) {
        ItemStack realMainHand = owner.getItemInHand(InteractionHand.MAIN_HAND);
        owner.setItemInHand(InteractionHand.MAIN_HAND, rapierStack.copy());
        AmethystRapierItem.setResolvingProjectileHit(true);

        try {
            owner.attack(target);
        } finally {
            AmethystRapierItem.setResolvingProjectileHit(false);
            owner.setItemInHand(InteractionHand.MAIN_HAND, realMainHand);
        }
    }
}
