package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.entity.BloodMistEntity;
import com.CoffeDino.lunacy.events.ErinyesCooldownAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

public class ErinyesItem extends SpellbladeItem {
    public static final int SPECIAL_ABILITY_COOLDOWN_TICKS = 1800;

    public ErinyesItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(new BloodMistEntity(serverLevel, player));
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.6f, 1.6f);
            ErinyesCooldownAttachments.applyCooldown(player, this, level.getGameTime());
        } else {
            player.getCooldowns().addCooldown(this, SPECIAL_ABILITY_COOLDOWN_TICKS);
        }

        player.getCooldowns().addCooldown(this, SPECIAL_ABILITY_COOLDOWN_TICKS);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }


}