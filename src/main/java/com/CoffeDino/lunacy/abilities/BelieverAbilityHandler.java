package com.CoffeDino.lunacy.abilities;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = Lunacy.MODID)
public class BelieverAbilityHandler {
    private static final Map<UUID, BelieverBarrierInstance> ACTIVE_BARRIERS = new HashMap<>();
    private static final int BARRIER_SIZE = 10;
    private static final int NIGHT_DURATION_TICKS = 10000;
    private static final long COOLDOWN_DURATION = 30000;
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final BlockState GLASS_LAYER = Blocks.YELLOW_STAINED_GLASS.defaultBlockState();
    private static final BlockState BARRIER_LAYER = Blocks.BARRIER.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public static void toggleAbility(ServerPlayer player, boolean isShiftDown) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        BelieverBarrierInstance barrier = ACTIVE_BARRIERS.get(playerId);

        if (barrier != null) {
            if (isShiftDown) {
                barrier.exitBarrier();
                Lunacy.LOGGER.debug("Player exited their believer barrier: {}", player.getName().getString());
            } else {
                deactivateAbility(player);
                Lunacy.LOGGER.debug("Believer barrier manually deactivated for player: {}", player.getName().getString());
            }
        } else {
            activateAbility(player);
        }
    }

    public static void activateAbility(ServerPlayer player) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        if (ACTIVE_BARRIERS.containsKey(playerId) || !canActivateAbility(player)) {
            return;
        }

        BelieverBarrierInstance barrier = new BelieverBarrierInstance(player);
        if (barrier.createBarrier()) {
            ACTIVE_BARRIERS.put(playerId, barrier);
            startCooldown(player);

            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Believer's Barrier activated for " + (NIGHT_DURATION_TICKS / 1200) + " minutes!"),
                    true
            );

            Lunacy.LOGGER.debug("Believer barrier activated for player: {}", player.getName().getString());
        }
    }

    public static void deactivateAbility(Player player) {
        UUID playerId = player.getUUID();
        BelieverBarrierInstance barrier = ACTIVE_BARRIERS.remove(playerId);

        if (barrier != null) {
            barrier.removeBarrier();
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Believer's Barrier deactivated!"),
                    true
            );

            Lunacy.LOGGER.debug("Believer barrier deactivated for player: {}", player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<UUID, BelieverBarrierInstance>> iterator = ACTIVE_BARRIERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, BelieverBarrierInstance> entry = iterator.next();
            BelieverBarrierInstance barrier = entry.getValue();

            if (barrier.tick() || !barrier.isValid()) {
                iterator.remove();
                barrier.removeBarrier();
                if (barrier.hasExpired()) {
                    barrier.getPlayer().displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Believer's Barrier has expired!"),
                            true
                    );
                }

                Lunacy.LOGGER.debug("Believer barrier expired for player: {}", barrier.getPlayer().getName().getString());
            }
        }
    }

    public static boolean isAbilityActive(Player player) {
        return ACTIVE_BARRIERS.containsKey(player.getUUID());
    }

    public static boolean canActivateAbility(Player player) {
        UUID playerId = player.getUUID();
        Long lastUsed = COOLDOWNS.get(playerId);

        if (lastUsed == null) {
            return true;
        }

        return System.currentTimeMillis() - lastUsed >= COOLDOWN_DURATION;
    }

    public static void startCooldown(Player player) {
        COOLDOWNS.put(player.getUUID(), System.currentTimeMillis());
    }

    private static class BelieverBarrierInstance {
        private final ServerPlayer player;
        private final ServerLevel level;
        private final Set<BlockPos> barrierBlocks;
        private final Map<BlockPos, BlockState> originalBlocks;
        private BlockPos centerPos;
        private AABB interiorBounds;
        private int ticksActive = 0;
        private boolean expired = false;

        public BelieverBarrierInstance(ServerPlayer player) {
            this.player = player;
            this.level = (ServerLevel) player.level();
            this.barrierBlocks = new HashSet<>();
            this.originalBlocks = new HashMap<>();
        }

        public boolean createBarrier() {
            this.centerPos = player.blockPosition();
            int startY = centerPos.getY()-1;
            int startX = centerPos.getX() - BARRIER_SIZE / 2;
            int startZ = centerPos.getZ() - BARRIER_SIZE / 2;

            this.interiorBounds = new AABB(
                    startX + 1, startY + 1, startZ + 1,
                    startX + BARRIER_SIZE - 1, startY + BARRIER_SIZE - 1, startZ + BARRIER_SIZE - 1
            );

            for (int x = 0; x < BARRIER_SIZE; x++) {
                for (int y = 0; y < BARRIER_SIZE; y++) {
                    for (int z = 0; z < BARRIER_SIZE; z++) {
                        boolean isWall = (x == 0 || x == BARRIER_SIZE - 1) ||
                                (z == 0 || z == BARRIER_SIZE - 1) ||
                                (y == 0 || y == BARRIER_SIZE - 1);

                        if (isWall) {
                            BlockPos innerPos = new BlockPos(startX + x, startY + y, startZ + z);
                            if (isReplaceable(innerPos)) {
                                replaceBlock(innerPos, GLASS_LAYER);
                                barrierBlocks.add(innerPos);
                            }
                            createOuterBarrierLayer(startX, startY, startZ, x, y, z);
                        } else {
                            BlockPos interiorPos = new BlockPos(startX + x, startY + y, startZ + z);
                            if (isReplaceable(interiorPos)) {
                                replaceBlock(interiorPos, AIR);
                                barrierBlocks.add(interiorPos);
                            }
                        }
                    }
                }
            }

            Lunacy.LOGGER.debug("Created double-layered believer barrier with {} blocks for player: {}",
                    barrierBlocks.size(), player.getName().getString());

            return !barrierBlocks.isEmpty();
        }

        private void createOuterBarrierLayer(int startX, int startY, int startZ, int x, int y, int z) {

            if (x == 0) {
                BlockPos outerPos = new BlockPos(startX + x - 1, startY + y, startZ + z);
                if (isReplaceable(outerPos)) {
                    replaceBlock(outerPos, BARRIER_LAYER);
                    barrierBlocks.add(outerPos);
                }
            }
            if (x == BARRIER_SIZE - 1) {
                BlockPos outerPos = new BlockPos(startX + x + 1, startY + y, startZ + z);
                if (isReplaceable(outerPos)) {
                    replaceBlock(outerPos, BARRIER_LAYER);
                    barrierBlocks.add(outerPos);
                }
            }
            if (z == 0) {
                BlockPos outerPos = new BlockPos(startX + x, startY + y, startZ + z - 1);
                if (isReplaceable(outerPos)) {
                    replaceBlock(outerPos, BARRIER_LAYER);
                    barrierBlocks.add(outerPos);
                }
            }
            if (z == BARRIER_SIZE - 1) {
                BlockPos outerPos = new BlockPos(startX + x, startY + y, startZ + z + 1);
                if (isReplaceable(outerPos)) {
                    replaceBlock(outerPos, BARRIER_LAYER);
                    barrierBlocks.add(outerPos);
                }
            }
            if (y == 0) {
                BlockPos outerPos = new BlockPos(startX + x, startY + y - 1, startZ + z);
                if (isReplaceable(outerPos)) {
                    replaceBlock(outerPos, BARRIER_LAYER);
                    barrierBlocks.add(outerPos);
                }
            }
            if (y == BARRIER_SIZE - 1) {
                BlockPos outerPos = new BlockPos(startX + x, startY + y + 1, startZ + z);
                if (isReplaceable(outerPos)) {
                    replaceBlock(outerPos, BARRIER_LAYER);
                    barrierBlocks.add(outerPos);
                }
            }
        }

        private boolean isReplaceable(BlockPos pos) {
            BlockState currentState = level.getBlockState(pos);
            if (pos.equals(player.blockPosition())) {
                return false;
            }

            return currentState.isAir() ||
                    currentState.getBlock() == Blocks.GRASS_BLOCK ||
                    currentState.getBlock() == Blocks.TALL_GRASS ||
                    currentState.getBlock() == Blocks.FERN ||
                    currentState.getBlock() == Blocks.DEAD_BUSH ||
                    currentState.getBlock() == Blocks.VINE ||
                    currentState.getDestroySpeed(level, pos) >= 0;
        }

        private boolean replaceBlock(BlockPos pos, BlockState newState) {
            BlockState currentState = level.getBlockState(pos);
            if (currentState.getBlock() == Blocks.BEDROCK ||
                    !currentState.getFluidState().isEmpty()) {
                return false;
            }
            if (pos.equals(player.blockPosition())) {
                return false;
            }
            originalBlocks.put(pos, currentState);
            level.setBlock(pos, newState, 3);
            return true;
        }

        public void removeBarrier() {
            evacuateInterior();

            for (BlockPos pos : barrierBlocks) {
                BlockState originalState = originalBlocks.get(pos);
                BlockState targetState = originalState != null ? originalState : AIR;
                level.setBlock(pos, targetState, 3);
            }

            barrierBlocks.clear();
            originalBlocks.clear();
        }

        private void evacuateInterior() {
            if (interiorBounds == null) return;
            if (player.isAlive() && !player.isRemoved() && interiorBounds.contains(player.position())) {
                BlockPos safePos = findSafeExitPosition();
                player.teleportTo(level, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5,
                        player.getYRot(), player.getXRot());
            }
            List<LivingEntity> occupants = level.getEntitiesOfClass(
                    LivingEntity.class, interiorBounds, entity -> entity != player);

            for (LivingEntity occupant : occupants) {
                BlockPos safePos = findSafeExitPosition();
                occupant.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
            }
        }

        public void exitBarrier() {
            BlockPos safePos = findSafeExitPosition();
            player.teleportTo(level, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5,
                    player.getYRot(), player.getXRot());
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("You stepped outside your Believer's Barrier!"),
                    true
            );
        }

        private BlockPos findSafeExitPosition() {
            int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            int radius = BARRIER_SIZE / 2 + 2;

            for (int[] dir : directions) {
                BlockPos candidate = new BlockPos(
                        centerPos.getX() + dir[0] * radius,
                        centerPos.getY(),
                        centerPos.getZ() + dir[1] * radius
                );
                BlockPos safe = scanForSafeSpot(candidate);
                if (safe != null) {
                    return safe;
                }
            }
            BlockPos above = new BlockPos(centerPos.getX(), centerPos.getY() + BARRIER_SIZE + 2, centerPos.getZ());
            BlockPos safeAbove = scanForSafeSpot(above);
            return safeAbove != null ? safeAbove : above;
        }

        private BlockPos scanForSafeSpot(BlockPos start) {
            for (int yOffset = 0; yOffset < 10; yOffset++) {
                BlockPos feet = start.above(yOffset);
                BlockPos head = feet.above();
                BlockPos ground = feet.below();

                boolean feetClear = level.getBlockState(feet).getCollisionShape(level, feet).isEmpty();
                boolean headClear = level.getBlockState(head).getCollisionShape(level, head).isEmpty();
                boolean groundSolid = !level.getBlockState(ground).getCollisionShape(level, ground).isEmpty();

                if (feetClear && headClear && groundSolid) {
                    return feet;
                }
            }
            return null;
        }

        public boolean tick() {
            if (!player.isAlive() || player.level().isClientSide()) {
                return true;
            }

            ticksActive++;
            if (ticksActive >= NIGHT_DURATION_TICKS) {
                expired = true;
                return true;
            }

            if (ticksActive % 20 == 0 && interiorBounds.contains(player.position())) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 1, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1, false, true, true));
            }

            return false;
        }

        public boolean isValid() {
            return player != null && player.isAlive() && !player.isRemoved();
        }

        public boolean hasExpired() {
            return expired;
        }

        public Player getPlayer() {
            return player;
        }
    }

    public static void onPlayerLogout(Player player) {
        deactivateAbility(player);
        Lunacy.LOGGER.debug("Believer barrier removed due to player logout: {}", player.getName().getString());
    }

    public static void onRaceChange(Player player) {
        deactivateAbility(player);
        Lunacy.LOGGER.debug("Believer barrier removed due to race change: {}", player.getName().getString());
    }
}