package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.entity.RocaBoulderEntity;
import com.CoffeDino.lunacy.events.RocaCooldownAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class RocaItem extends SwordItem {

    public static final int STANCE_DURATION_TICKS = 300;
    public static final int STANCE_COOLDOWN_TICKS = 600;

    private static final int BLOCK_SEARCH_RADIUS = 5;
    private static final float BOULDER_SPEED = 1.8F;
    private static final float BASE_DAMAGE = 6.0F;
    private static final float HARDNESS_MULTIPLIER = 3.0F;

    public RocaItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            long gameTime = level.getGameTime();
            player.setData(ModAttachments.ROCA_STANCE_END, gameTime + STANCE_DURATION_TICKS);
            RocaCooldownAttachments.applyCooldown(player, this, gameTime);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.6F, 1.6F);
        }

        return InteractionResultHolder.success(stack);
    }

    public static boolean tryTriggerBoulder(ServerPlayer player, LivingEntity target) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof RocaItem)) {
            return false;
        }

        long stanceEnd = player.getData(ModAttachments.ROCA_STANCE_END);
        long gameTime = player.serverLevel().getGameTime();
        if (stanceEnd <= 0 || gameTime > stanceEnd) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        BlockPos blockPos = findRandomNearbyBlock(level, target.blockPosition());
        if (blockPos == null) {
            return false;
        }

        BlockState state = level.getBlockState(blockPos);
        float hardness = state.getDestroySpeed(level, blockPos);
        float damage = BASE_DAMAGE + hardness * HARDNESS_MULTIPLIER;

        level.removeBlock(blockPos, false);

        Vec3 spawnPos = Vec3.atCenterOf(blockPos);

        RocaBoulderEntity boulder = new RocaBoulderEntity(level, player, spawnPos, target, state, damage, BOULDER_SPEED);
        level.addFreshEntity(boulder);

        level.playSound(null, blockPos, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.0F, 0.9F);

        return true;
    }

    private static BlockPos findRandomNearbyBlock(ServerLevel level, BlockPos center) {
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-BLOCK_SEARCH_RADIUS, -BLOCK_SEARCH_RADIUS, -BLOCK_SEARCH_RADIUS),
                center.offset(BLOCK_SEARCH_RADIUS, BLOCK_SEARCH_RADIUS, BLOCK_SEARCH_RADIUS))) {
            BlockState state = level.getBlockState(pos);

            if (state.isAir()) continue;
            if (state.getBlock() instanceof LiquidBlock) continue;

            float hardness = state.getDestroySpeed(level, pos);
            if (hardness < 0) continue;

            if (!isExposedToAir(level, pos)) continue;

            candidates.add(pos.immutable());
        }

        if (candidates.isEmpty()) return null;
        return candidates.get(level.getRandom().nextInt(candidates.size()));
    }

    private static boolean isExposedToAir(ServerLevel level, BlockPos pos) {
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.getCollisionShape(level, neighbor).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}