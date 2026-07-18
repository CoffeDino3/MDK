package com.CoffeDino.lunacy.abilities;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Lunacy.MODID)
public class EtherealAbilityHandler {
    private static final Map<UUID, EtherealAbilityInstance> ACTIVE_ABILITIES = new HashMap<>();
    private static final Map<UUID, Boolean> PLAYER_JUMPING = new HashMap<>();
    private static final Map<UUID, Boolean> PLAYER_SHIFTING = new HashMap<>();

    public static void activateAbility(Player player, boolean jumping, boolean shifting) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        if (ACTIVE_ABILITIES.containsKey(playerId) || !canActivateAbility(player)) {
            return;
        }
        PLAYER_JUMPING.put(playerId, jumping);
        PLAYER_SHIFTING.put(playerId, shifting);

        EtherealAbilityInstance ability = new EtherealAbilityInstance((ServerPlayer) player);
        ACTIVE_ABILITIES.put(playerId, ability);
        startCooldown(player);

        Lunacy.LOGGER.debug("Ethereal ability activated for player: {}", player.getName().getString());
    }
    public static void updateEtherealInput(ServerPlayer player, boolean jumping, boolean shifting) {
        if (isAbilityActive(player)) {
            UUID playerId = player.getUUID();
            PLAYER_JUMPING.put(playerId, jumping);
            PLAYER_SHIFTING.put(playerId, shifting);
        }
    }
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_DURATION = 30000;
    private static final int ABILITY_DURATION = 200;
    private static final float MOVE_SPEED = 0.1f;
    private static final float VERTICAL_SPEED = 0.2f;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ACTIVE_ABILITIES.values().forEach(EtherealAbilityInstance::tick);
        ACTIVE_ABILITIES.entrySet().removeIf(entry -> {
            EtherealAbilityInstance ability = entry.getValue();
            if (ability.shouldEnd()) {
                ability.deactivate();
                Lunacy.LOGGER.debug("Ethereal ability ended for player: {}", ability.getPlayer().getName().getString());
                return true;
            }
            return false;
        });
    }

    public static boolean isAbilityActive(Player player) {
        return ACTIVE_ABILITIES.containsKey(player.getUUID());
    }

    public static void deactivateAbility(Player player) {
        UUID playerId = player.getUUID();
        EtherealAbilityInstance ability = ACTIVE_ABILITIES.remove(playerId);
        if (ability != null) {
            ability.deactivate();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.teleport(
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        serverPlayer.getYRot(), serverPlayer.getXRot());
            }
        }
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
    public static boolean canPhaseThroughBlocks(Player player) {
        return isAbilityActive(player);
    }

    private static class EtherealAbilityInstance {
        private final ServerPlayer player;
        private final ServerLevel level;
        private int ticksActive = 0;
        private boolean isActive = false;
        private Vec3 safePosition;
        private boolean wasInBlock = false;
        private GameType originalGameType;
        private boolean wasFlyingBeforeAbility;

        public EtherealAbilityInstance(ServerPlayer player) {
            this.player = player;
            this.level = (ServerLevel) player.level();
            this.safePosition = player.position();
            activate();
        }

        public void activate() {
            if (!player.isAlive()) return;
            this.safePosition = player.position();
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, ABILITY_DURATION, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ABILITY_DURATION, 0, false, false));
            this.originalGameType = player.gameMode.getGameModeForPlayer();
            this.wasFlyingBeforeAbility = player.getAbilities().flying;
            player.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);

            this.isActive = true;

            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Ethereal Form activated! You can phase through blocks for 10 seconds."),
                    true
            );

            Lunacy.LOGGER.info("Ethereal ability activated for {}", player.getName().getString());
        }

        public void tick() {
            if (!isActive || !player.isAlive()) return;
            ticksActive++;

            boolean currentlyInBlock = !level.isEmptyBlock(player.blockPosition());
            if (!currentlyInBlock) {
                safePosition = player.position();
                wasInBlock = false;
            } else {
                wasInBlock = true;
            }

            if (ticksActive >= ABILITY_DURATION) {
                endAbility();
            }
        }

        private void endAbility() {
            Vec3 safeExit = findNearestSafePosition();
            deactivate();

            if (safeExit != null) {
                player.teleportTo(safeExit.x, safeExit.y, safeExit.z);
            } else {
                emergencyEscapeFromBlocks();
            }
            player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        }

        public void deactivate() {
            if (!isActive) return;
            player.removeEffect(MobEffects.GLOWING);
            player.removeEffect(MobEffects.INVISIBILITY);
            player.gameMode.changeGameModeForPlayer(originalGameType);
            player.getAbilities().flying = player.getAbilities().mayfly && wasFlyingBeforeAbility;
            player.onUpdateAbilities();
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;

            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Ethereal Form ended."),
                    true
            );

            Lunacy.LOGGER.info("Ethereal ability ended for {}", player.getName().getString());
            isActive = false;
        }

        private void emergencyEscapeFromBlocks() {
            BlockPos currentPos = player.blockPosition();

            if (!level.isEmptyBlock(currentPos)) {
                for (int radius = 0; radius <= 5; radius++) {
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (radius != 0 && Math.abs(x) != radius && Math.abs(z) != radius) continue;
                            for (int y = -3; y <= 6; y++) {
                                BlockPos checkPos = currentPos.offset(x, y, z);
                                if (isPositionSafeWithAir(checkPos)) {
                                    player.teleportTo(checkPos.getX() + 0.5, checkPos.getY(), checkPos.getZ() + 0.5);
                                    player.resetFallDistance();
                                    return;
                                }
                            }
                        }
                    }
                }
                BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, currentPos);
                player.teleportTo(surfacePos.getX() + 0.5, surfacePos.getY(), surfacePos.getZ() + 0.5);
                player.resetFallDistance();
            }
        }

        private Vec3 findNearestSafePosition() {
            BlockPos center = player.blockPosition();
            for (int y = -5; y <= 5; y++) {
                for (int x = -5; x <= 5; x++) {
                    for (int z = -5; z <= 5; z++) {
                        BlockPos checkPos = center.offset(x, y, z);
                        if (isPositionSafeWithAir(checkPos)) {
                            return Vec3.atBottomCenterOf(checkPos).add(0, 0.5, 0);
                        }
                    }
                }
            }

            return null;
        }

        private boolean isPositionSafeWithAir(BlockPos pos) {
            return level.isEmptyBlock(pos) &&
                    level.isEmptyBlock(pos.above()) &&
                    level.isEmptyBlock(pos.above(2)) &&
                    !level.isEmptyBlock(pos.below());
        }

        public boolean shouldEnd() {
            return ticksActive >= ABILITY_DURATION || !player.isAlive() || !isActive;
        }

        public Player getPlayer() {
            return player;
        }
    }
}