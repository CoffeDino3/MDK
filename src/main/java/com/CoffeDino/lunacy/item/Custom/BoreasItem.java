package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.entity.BoreasStormEntity;
import com.CoffeDino.lunacy.events.BoreasCooldownAttachments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BoreasItem extends SpellbladeItem {

    public static final int SPECIAL_ABILITY_COOLDOWN_TICKS = 1200;

    public BoreasItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            Vec3 spawnPos = player.position();
            level.addFreshEntity(new BoreasStormEntity(level, spawnPos, player));
            level.playSound(null, player.blockPosition(), SoundEvents.WEATHER_RAIN, SoundSource.PLAYERS, 0.7f, 0.6f);
            BoreasCooldownAttachments.applyCooldown(player, this, level.getGameTime());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}