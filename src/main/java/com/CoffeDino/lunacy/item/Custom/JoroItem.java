package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.client.JoroArmedState;
import com.CoffeDino.lunacy.events.JoroCooldownAttachments;
import com.CoffeDino.lunacy.renderer.JoroDrillRenderer;
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

public class JoroItem extends SpellbladeItem {

    public static final int MIN_DRILL_GAP_TICKS = 6;
    public static final int DRILL_DEPTH = 3;
    public static final int SPECIAL_ABILITY_COOLDOWN_TICKS = 1800;
    public static final int CHARGE_TICKS = 60;
    public static final int ARMED_WINDOW_TICKS = 500;

    public JoroItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return CHARGE_TICKS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (!level.isClientSide) return;
        int elapsed = CHARGE_TICKS - remainingUseTicks;
        JoroDrillRenderer.spawnVortexTick(entity, elapsed);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        long armedUntil = level.getGameTime() + ARMED_WINDOW_TICKS;
        if (!level.isClientSide && entity instanceof Player player) {
            JoroCooldownAttachments.applyCooldown(player, this, level.getGameTime());
            player.setData(ModAttachments.JORO_ARMED_END.get(), armedUntil);
            level.playSound(null, entity.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 1.2f);
        }
        if (level.isClientSide) {
            JoroArmedState.markArmed(entity, armedUntil);
        }
        return stack;
    }
}