package com.CoffeDino.lunacy.abilities;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@EventBusSubscriber(modid = Lunacy.MODID)
public class EnderAbilityHandler {
    private static final int POST_TELEPORT_BUFF_WINDOW_TICKS = 60;
    private static final float POST_TELEPORT_DAMAGE_MULTIPLIER = 1.5f;
    private static final int DODGE_UNLOCK_LEVEL = 30;
    private static final float DODGE_CHANCE = 0.20f;
    private static final int DODGE_MAX_DISTANCE = 7;

    private static final Map<UUID, Long> postTeleportBuffExpiry = new HashMap<>();

    public static void grantPostTeleportBuff(ServerPlayer player) {
        long expiryTick = player.level().getGameTime() + POST_TELEPORT_BUFF_WINDOW_TICKS;
        postTeleportBuffExpiry.put(player.getUUID(), expiryTick);
    }

    public static boolean hasPostTeleportBuff(ServerPlayer player) {
        Long expiry = postTeleportBuffExpiry.get(player.getUUID());
        if (expiry == null) return false;
        return player.level().getGameTime() <= expiry;
    }
    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker
                && races.getPlayerRace(attacker) == races.Race.ENDER
                && hasPostTeleportBuff(attacker)) {
            postTeleportBuffExpiry.remove(attacker.getUUID());
            event.setAmount(event.getAmount() * POST_TELEPORT_DAMAGE_MULTIPLIER);
        }
        if (event.getEntity() instanceof ServerPlayer victim
                && races.getPlayerRace(victim) == races.Race.ENDER) {
            int level = PlayerLevels.getLevel(victim);
            if (level >= DODGE_UNLOCK_LEVEL && Math.random() <= DODGE_CHANCE) {
                BlockPos dodgePos = findDodgePosition(victim);
                event.setCanceled(true);

                if (dodgePos != null) {
                    ServerLevel level1 = (ServerLevel) victim.level();
                    level1.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    victim.teleportTo(dodgePos.getX() + 0.5, dodgePos.getY(), dodgePos.getZ() + 0.5);
                    level1.playSound(null, dodgePos.getX(), dodgePos.getY(), dodgePos.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }

    private static BlockPos findDodgePosition(ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        java.util.List<int[]> shuffled = new java.util.ArrayList<>(java.util.List.of(directions));
        java.util.Collections.shuffle(shuffled);

        for (int[] dir : shuffled) {
            for (int dist = DODGE_MAX_DISTANCE; dist >= 1; dist--) {
                BlockPos candidate = origin.offset(dir[0] * dist, 0, dir[1] * dist);
                if (isSameLevelSafeSpot(player, candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean isSameLevelSafeSpot(ServerPlayer player, BlockPos feet) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos head = feet.above();
        BlockPos ground = feet.below();

        boolean feetClear = level.getBlockState(feet).getCollisionShape(level, feet).isEmpty();
        boolean headClear = level.getBlockState(head).getCollisionShape(level, head).isEmpty();
        boolean groundSolid = !level.getBlockState(ground).getCollisionShape(level, ground).isEmpty();

        return feetClear && headClear && groundSolid;
    }

    public static void clear(ServerPlayer player) {
        postTeleportBuffExpiry.remove(player.getUUID());
    }
}

