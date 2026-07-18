package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.events.ObsidiaCooldownAttachments;
import com.CoffeDino.lunacy.handlers.BlastJobManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ObsidiaItem extends DaggerItem {

    public static final int SPECIAL_ABILITY_COOLDOWN_TICKS = 600;
    private static final int USE_DURATION_TICKS = 72000;
    private static final int MIN_CHARGE_TICKS = 15;
    private static final int MAX_CHARGE_TICKS = 60;
    private static final double MIN_RANGE = 10.0;
    private static final double MAX_RANGE = 50.0;
    private static final double CONE_HALF_ANGLE_DEG = 12.0;
    private static final double START_OFFSET = 2.0;
    private static final int CLEAR_ABOVE = 200;
    private static final int MIN_DOWN_DEPTH = 3;
    private static final int MAX_DOWN_DEPTH = 30;
    private static final int LAVA_LIFETIME_TICKS = 100;

    public ObsidiaItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    public ObsidiaItem(Tier tier, Properties properties) {
        this(tier, 5.0f, -1.4f, properties);
    }

    @Override
    public float getBackstabMultiplier() {
        return 2.5f;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) return;

        int chargedTicks = getUseDuration(stack, livingEntity) - timeLeft;
        if (chargedTicks < MIN_CHARGE_TICKS) return;

        if (!(level instanceof ServerLevel serverLevel)) return;

        double chargeRatio = Mth.clamp(
                (double) (chargedTicks - MIN_CHARGE_TICKS) / (MAX_CHARGE_TICKS - MIN_CHARGE_TICKS),
                0.0, 1.0
        );
        double range = Mth.lerp(chargeRatio, MIN_RANGE, MAX_RANGE);

        Vec3 origin = player.position();
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0, look.z);
        if (forward.lengthSqr() < 1.0E-4) forward = new Vec3(0, 0, 1);
        forward = forward.normalize();

        BlastJobManager.queueBlast(
                serverLevel, player, origin, forward, range,
                START_OFFSET, CONE_HALF_ANGLE_DEG,
                CLEAR_ABOVE, MIN_DOWN_DEPTH, MAX_DOWN_DEPTH,
                LAVA_LIFETIME_TICKS
        );

        ObsidiaCooldownAttachments.applyCooldown(player, this, serverLevel.getGameTime());

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tip, flag);
        tip.add(Component.translatable("item.lunacy.obsidia.ability")
                .withStyle(ChatFormatting.DARK_RED));
    }
}