package net.CoffeDino.testmod.abilities;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = Lunacy.MOD_ID)
public class BelieverAbilityHandler {
    private static final Map<UUID, BelieverBarrierInstance> ACTIVE_BARRIERS = new HashMap<>();
    private static final int BARRIER_SIZE = 10;
    private static final int NIGHT_DURATION_TICKS = 10000;
    private static final long COOLDOWN_DURATION = 30000;
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final BlockState GLASS_LAYER = Blocks.YELLOW_STAINED_GLASS.defaultBlockState();
    private static final BlockState BARRIER_LAYER = Blocks.BARRIER.defaultBlockState();

    public static void toggleAbility(ServerPlayer player) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        if (ACTIVE_BARRIERS.containsKey(playerId)) {
            deactivateAbility(player);
            Lunacy.LOGGER.debug("Believer barrier manually deactivated for player: {}", player.getName().getString());
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
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

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
            for (BlockPos pos : barrierBlocks) {
                BlockState originalState = originalBlocks.get(pos);
                if (originalState != null) {
                    level.setBlock(pos, originalState, 3);
                } else {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }

            barrierBlocks.clear();
            originalBlocks.clear();
        }

        public boolean tick() {
            if (!player.isAlive() || player.level().isClientSide()) {
                return true;
            }

            ticksActive++;

            // Check if duration has expired
            if (ticksActive >= NIGHT_DURATION_TICKS) {
                expired = true;
                return true;
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