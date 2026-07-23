package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.item.Custom.JoroItem;
import com.CoffeDino.lunacy.capability.ModAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

@EventBusSubscriber(modid = "lunacy")
public class JoroAbilityHandler {
    private static final float DAMAGE_PERCENT_OF_MAX_HEALTH = 0.10f;
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof JoroItem)) return;

        long now = player.level().getGameTime();
        long armedEnd = player.getData(ModAttachments.JORO_ARMED_END.get());
        if (now >= armedEnd) return;
        event.setCanceled(true);
        long lastDrill = player.getData(ModAttachments.JORO_LAST_DRILL_TICK.get());
        if (now - lastDrill < JoroItem.MIN_DRILL_GAP_TICKS) return;
        player.setData(ModAttachments.JORO_LAST_DRILL_TICK.get(), now);
        drill(player, event.getPos(), event.getFace());
    }
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof JoroItem)) return;
        long now = player.level().getGameTime();
        long armedEnd = player.getData(ModAttachments.JORO_ARMED_END.get());
        if (now >= armedEnd) return;
        long lastDrill = player.getData(ModAttachments.JORO_LAST_DRILL_TICK.get());
        if (now - lastDrill < JoroItem.MIN_DRILL_GAP_TICKS) return;
        player.setData(ModAttachments.JORO_LAST_DRILL_TICK.get(), now);
        BlockPos origin = event.getTarget().blockPosition();
        Vec3 toTarget = event.getTarget().position().subtract(player.position());
        Direction into = Direction.getNearest(toTarget.x, toTarget.y, toTarget.z);
        drill(player, origin, into.getOpposite());
    }
    private static final int[] BAND_DEPTHS = {3, 2, 1};
    private static final int[] BAND_HALF   = {2, 1, 0};

    private static void drill(ServerPlayer player, BlockPos origin, Direction hitFace) {
        Level level = player.level();
        Direction into = hitFace.getOpposite();
        Direction axisA = perpendicularA(into);
        Direction axisB = perpendicularB(into);
        int depth = 0;
        for (int band = 0; band < BAND_DEPTHS.length; band++) {
            int half = BAND_HALF[band];
            for (int i = 0; i < BAND_DEPTHS[band]; i++, depth++) {
                BlockPos center = origin.relative(into, depth);
                for (int a = -half; a <= half; a++) {
                    for (int b = -half; b <= half; b++) {
                        BlockPos pos = center.relative(axisA, a).relative(axisB, b);
                        if (!level.isInWorldBounds(pos)) continue;
                        BlockState state = level.getBlockState(pos);
                        if (state.isAir()) continue;
                        if (state.getDestroySpeed(level, pos) < 0) continue;
                        level.destroyBlock(pos, true, player);
                    }
                }
            }
        }

        level.playSound(null, origin, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
        knockbackAndDamage(level, player, origin, into);
    }

    private static void knockbackAndDamage(Level level, ServerPlayer player, BlockPos origin, Direction into) {
        Vec3 center = Vec3.atCenterOf(origin)
                .add(Vec3.atLowerCornerOf(into.getNormal()).scale(JoroItem.DRILL_DEPTH));
        AABB area = new AABB(center, center).inflate(4.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        for (LivingEntity target : targets) {
            Vec3 push = target.position().subtract(player.position()).normalize().scale(1.6).add(0, 0.35, 0);
            target.setDeltaMovement(target.getDeltaMovement().add(push));
            target.hurtMarked = true;
            float damage = target.getMaxHealth() * DAMAGE_PERCENT_OF_MAX_HEALTH;
            target.hurt(level.damageSources().playerAttack(player), damage);
        }
    }

    private static Direction perpendicularA(Direction facing) {
        return switch (facing.getAxis()) {
            case X -> Direction.UP;
            case Y, Z -> Direction.EAST;
        };
    }

    private static Direction perpendicularB(Direction facing) {
        return switch (facing.getAxis()) {
            case X, Y -> Direction.SOUTH;
            case Z -> Direction.UP;
        };
    }
}