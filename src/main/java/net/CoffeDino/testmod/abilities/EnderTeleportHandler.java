package net.CoffeDino.testmod.abilities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EnderTeleportHandler {
    private static final int MAX_TELEPORT_DISTANCE = 25;
    private static final int COOLDOWN_TICKS = 60;

    public static void teleportPlayer(ServerPlayer player) {
        if (player.getCooldowns().isOnCooldown(player.getUseItem().getItem())) {
            return;
        }

        Vec3 teleportPos = calculateTeleportPosition(player);

        if (teleportPos != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
            player.level().playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            player.getCooldowns().addCooldown(player.getUseItem().getItem(), COOLDOWN_TICKS);
        }
    }

    private static Vec3 calculateTeleportPosition(Player player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);
        Vec3 reachVector = eyePosition.add(viewVector.x * MAX_TELEPORT_DISTANCE,
                viewVector.y * MAX_TELEPORT_DISTANCE,
                viewVector.z * MAX_TELEPORT_DISTANCE);
        BlockHitResult hitResult = player.level().clip(new ClipContext(
                eyePosition,
                reachVector,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return adjustTeleportPosition(player.level(), hitResult.getLocation(), player);
        } else {
            return adjustTeleportPosition(player.level(), reachVector, player);
        }
    }

    private static Vec3 adjustTeleportPosition(Level level, Vec3 targetPos, Player player) {
        double x = targetPos.x;
        double y = targetPos.y;
        double z = targetPos.z;
        BlockPos targetBlockPos = BlockPos.containing(x, y, z);
        BlockPos safePos = findSafeTeleportPosition(level, targetBlockPos);
        if (safePos != null) {
            return new Vec3(safePos.getX() + 0.5, safePos.getY() + 1.0, safePos.getZ() + 0.5);
        }
        return new Vec3(x, y + 1.0, z);
    }

    private static BlockPos findSafeTeleportPosition(Level level, BlockPos startPos) {
        if (isPositionSafe(level, startPos)) {
            return startPos;
        }
        for (int range = 1; range <= 3; range++) {
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = startPos.offset(x, 0, z);
                    if (isPositionSafe(level, checkPos)) {
                        return checkPos;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isPositionSafe(Level level, BlockPos pos) {
        BlockPos feetPos = pos.above();
        BlockPos headPos = feetPos.above();

        return level.getBlockState(pos).isSolid() &&
                level.isEmptyBlock(feetPos) &&
                level.isEmptyBlock(headPos);
    }
}