package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.entity.HeliosSphereEntity;
import com.CoffeDino.lunacy.events.HeliosCooldownAttachments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HeliosItem extends SpellbladeItem {

    public static final int SPECIAL_ABILITY_COOLDOWN_TICKS = 1200;
    private static final double SPAWN_HEIGHT_ABOVE_PLAYER = 10.0;

    public HeliosItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            Vec3 spawnPos = player.position().add(0, SPAWN_HEIGHT_ABOVE_PLAYER, 0);
            HeliosSphereEntity sphere = new HeliosSphereEntity(level, spawnPos, player);
            level.addFreshEntity(sphere);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 1.2f);
            HeliosCooldownAttachments.applyCooldown(player, this, level.getGameTime());
        } else {
            player.getCooldowns().addCooldown(this, SPECIAL_ABILITY_COOLDOWN_TICKS);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}