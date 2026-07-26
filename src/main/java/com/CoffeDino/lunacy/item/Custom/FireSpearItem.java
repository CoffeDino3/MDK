package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.domain.FireDomainManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class FireSpearItem extends SpearItem {

    public static final int COOLDOWN_TICKS = 1200;

    public FireSpearItem(Tier tier, Properties properties) {
        super(tier, 8.0f, -2.8f, 3.0f, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) return;
        FireDomainManager.beginOrContinue(player);
        if (remainingUseDuration == 71999) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.9F);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            FireDomainManager.release(player);
            applyCooldown(player, level.getGameTime());
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int remainingUseDuration) {
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer player) {
            FireDomainManager.release(player);
        }
    }

    private void applyCooldown(ServerPlayer player, long gameTime) {
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        player.setData(ModAttachments.FIRE_SPEAR_COOLDOWN_END, gameTime + COOLDOWN_TICKS);
    }
}