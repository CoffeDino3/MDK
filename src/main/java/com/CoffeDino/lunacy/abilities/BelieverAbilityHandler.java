package com.CoffeDino.lunacy.abilities;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.effects.AbilityCooldown;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
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
    private static final int BASE_BARRIER_SIZE = 10;
    private static final int BARRIER_GROWTH_PER_25_LEVELS = 2;
    private static final int NIGHT_DURATION_TICKS = 10000;
    private static final int COOLDOWN_TICKS = 600;
    private static final int DEBUFF_UNLOCK_LEVEL = 20;
    private static final int DEBUFF_LEVEL_INTERVAL = 20;
    private static final BlockState GLASS_LAYER = Blocks.YELLOW_STAINED_GLASS.defaultBlockState();
    private static final BlockState BARRIER_LAYER = Blocks.BARRIER.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private static int getBarrierSize(int level) {
        int growthTiers = level / 25;
        int size = BASE_BARRIER_SIZE + growthTiers * BARRIER_GROWTH_PER_25_LEVELS;
        if (size % 2 != 0) size++;
        return size;
    }

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

        if (ACTIVE_BARRIERS.containsKey(playerId) || AbilityCooldown.isActive(player, ModEffects.BELIEVER_COOLDOWN)) {
            return;
        }

        int level = PlayerLevels.getLevel(player);
        BelieverBarrierInstance barrier = new BelieverBarrierInstance(player, level);
        if (barrier.createBarrier()) {
            ACTIVE_BARRIERS.put(playerId, barrier);

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
            AbilityCooldown.start(player, ModEffects.BELIEVER_COOLDOWN, COOLDOWN_TICKS);
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
                AbilityCooldown.start(barrier.getPlayer(), ModEffects.BELIEVER_COOLDOWN, COOLDOWN_TICKS);
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

    private static class BelieverBarrierInstance {
        private final ServerPlayer player;
        private final ServerLevel level;
        private final Set<BlockPos> barrierBlocks;
        private final Map<BlockPos, BlockState> originalBlocks;
        private final int barrierSize;
        private final int playerLevel;
        private final int buffAmplifier;
        private final int weaknessAmplifier;
        private BlockPos centerPos;
        private AABB interiorBounds;
        private int ticksActive = 0;
        private boolean expired = false;

        public BelieverBarrierInstance(ServerPlayer player, int playerLevel) {
            this.player = player;
            this.level = (ServerLevel) player.level();
            this.barrierBlocks = new HashSet<>();
            this.originalBlocks = new HashMap<>();
            this.playerLevel = playerLevel;
            this.barrierSize = getBarrierSize(playerLevel);
            this.buffAmplifier = 1 + (playerLevel / 10);
            this.weaknessAmplifier = playerLevel >= DEBUFF_UNLOCK_LEVEL
                    ? (playerLevel / DEBUFF_LEVEL_INTERVAL)
                    : -1;
        }

        public boolean createBarrier() {
            this.centerPos = player.blockPosition();
            int startY = centerPos.getY()-1;
            int startX = centerPos.getX() - barrierSize / 2;
            int startZ = centerPos.getZ() - barrierSize / 2;

            this.interiorBounds = new AABB(
                    startX + 1, startY + 1, startZ + 1,
                    startX + barrierSize - 1, startY + barrierSize - 1, startZ + barrierSize - 1
            );

            for (int x = 0; x < barrierSize; x++) {
                for (int y = 0; y < barrierSize; y++) {
                    for (int z = 0; z < barrierSize; z++) {
                        boolean isWall = (x == 0 || x == barrierSize - 1) ||
                                (z == 0 || z == barrierSize - 1) ||
                                (y == 0 || y == barrierSize - 1);

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

            Lunacy.LOGGER.debug("Created double-layered believer barrier ({}^3) with {} blocks for player: {} (level {})",
                    barrierSize, barrierBlocks.size(), player.getName().getString(), playerLevel);

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
            if (x == barrierSize - 1) {
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
            if (z == barrierSize - 1) {
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
            if (y == barrierSize - 1) {
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
                    !currentState.getFluidState().isEmpty() ||
                    currentState.getDestroySpeed(level, pos) >= 0;
        }

        private boolean replaceBlock(BlockPos pos, BlockState newState) {
            BlockState currentState = level.getBlockState(pos);
            if (currentState.getBlock() == Blocks.BEDROCK) {
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
            int radius = barrierSize / 2 + 2;

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
            BlockPos above = new BlockPos(centerPos.getX(), centerPos.getY() + barrierSize + 2, centerPos.getZ());
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
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, buffAmplifier, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, buffAmplifier, false, true, true));
            }

            if (weaknessAmplifier >= 0 && ticksActive % 20 == 0) {
                applyWeaknessToIntruders();
            }

            return false;
        }

        private void applyWeaknessToIntruders() {
            List<LivingEntity> intruders = level.getEntitiesOfClass(
                    LivingEntity.class, interiorBounds, entity -> entity != player && entity.isAlive());

            for (LivingEntity intruder : intruders) {
                intruder.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, weaknessAmplifier, false, true, true));
            }
        }

        public boolean isValid() {
            return player != null && player.isAlive() && !player.isRemoved();
        }

        public boolean hasExpired() {
            return expired;
        }

        public ServerPlayer getPlayer() {
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
