package net.CoffeDino.testmod.abilities;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Lunacy.MOD_ID)
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
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
            player.noPhysics = true;
            player.setNoGravity(true);
            player.setInvulnerable(true);

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
            player.noPhysics = true;
            player.setNoGravity(true);
            player.setInvulnerable(true);
            handleEtherealMovement();
            boolean currentlyInBlock = !level.isEmptyBlock(player.blockPosition());
            if (!currentlyInBlock) {
                safePosition = player.position();
                wasInBlock = false;
            } else {
                wasInBlock = true;
                assistBlockPhasing();
            }
            if (ticksActive >= ABILITY_DURATION) {
                endAbility();
            }
            if (ticksActive % 20 == 0) {
                logDebugInfo(currentlyInBlock);
            }
        }

        private void handleEtherealMovement() {
            UUID playerId = player.getUUID();
            boolean jumping = PLAYER_JUMPING.getOrDefault(playerId, false);
            boolean shifting = PLAYER_SHIFTING.getOrDefault(playerId, false);

            double motionX = 0;
            double motionY = player.getDeltaMovement().y;
            double motionZ = 0;
            if (player.zza != 0 || player.xxa != 0) {
                float yawRadians = player.getYRot() * ((float)Math.PI / 180F);
                motionX = (-Math.sin(yawRadians) * player.zza + Math.cos(yawRadians) * player.xxa) * MOVE_SPEED;
                motionZ = (Math.cos(yawRadians) * player.zza + Math.sin(yawRadians) * player.xxa) * MOVE_SPEED;
            } else {
                motionX = player.getDeltaMovement().x * 0.8;
                motionZ = player.getDeltaMovement().z * 0.8;
            }
            if (jumping) {
                motionY = VERTICAL_SPEED;
            } else if (shifting) {
                motionY = -VERTICAL_SPEED;
            } else {
                motionY *= 0.9;
            }
            player.setDeltaMovement(motionX, motionY, motionZ);
            player.move(net.minecraft.world.entity.MoverType.SELF, player.getDeltaMovement());

            player.hurtMarked = true;
            player.fallDistance = 0.0f;
        }

        private void assistBlockPhasing() {
            BlockPos currentPos = player.blockPosition();
            if (!level.isEmptyBlock(currentPos) &&
                    !level.isEmptyBlock(currentPos.above()) &&
                    !level.isEmptyBlock(currentPos.below())) {
                float yawRadians = player.getYRot() * ((float)Math.PI / 180F);
                double pushX = -Math.sin(yawRadians) * 0.05;
                double pushZ = Math.cos(yawRadians) * 0.05;

                player.setDeltaMovement(
                        player.getDeltaMovement().x + pushX,
                        player.getDeltaMovement().y,
                        player.getDeltaMovement().z + pushZ
                );
                Vec3 newPos = player.position().add(player.getDeltaMovement());
                player.setPos(newPos.x, newPos.y, newPos.z);
            }
        }

        private void endAbility() {
            Vec3 safeExit = findNearestSafePosition();
            if (safeExit != null) {
                player.teleportTo(safeExit.x, safeExit.y, safeExit.z);
                Lunacy.LOGGER.debug("Teleported player to safe position at ability end");
            } else {
                Lunacy.LOGGER.debug("No safe position found, using emergency escape");
                emergencyEscapeFromBlocks();
            }

            deactivate();
        }

        public void deactivate() {
            if (!isActive) return;
            player.removeEffect(MobEffects.GLOWING);
            player.removeEffect(MobEffects.INVISIBILITY);
            player.noPhysics = false;
            player.setNoGravity(false);
            player.setInvulnerable(false);
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
                Lunacy.LOGGER.debug("Player stuck in block at ability end, attempting emergency escape");
                for (int i = 1; i <= 10; i++) {
                    BlockPos checkPos = currentPos.above(i);
                    if (level.isEmptyBlock(checkPos) && level.isEmptyBlock(checkPos.above())) {
                        player.teleportTo(player.getX(), player.getY() + i, player.getZ());
                        Lunacy.LOGGER.debug("Emergency escape: moved up {} blocks", i);
                        return;
                    }
                }
                for (int radius = 1; radius <= 5; radius++) {
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (Math.abs(x) == radius || Math.abs(z) == radius) {
                                for (int y = -2; y <= 2; y++) {
                                    BlockPos checkPos = currentPos.offset(x, y, z);
                                    if (level.isEmptyBlock(checkPos) && level.isEmptyBlock(checkPos.above())) {
                                        player.teleportTo(player.getX() + x, player.getY() + y, player.getZ() + z);
                                        Lunacy.LOGGER.debug("Emergency escape: moved to ({}, {}, {})", x, y, z);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                player.teleportTo(safePosition.x, safePosition.y, safePosition.z);
                Lunacy.LOGGER.debug("Emergency escape: teleported to original safe position");
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

        private void logDebugInfo(boolean inBlock) {
            Lunacy.LOGGER.debug(
                    "Ethereal [{}] - Pos: ({:.1f}, {:.1f}, {:.1f}), InBlock: {}, noPhysics: {}",
                    ticksActive, player.getX(), player.getY(), player.getZ(), inBlock, player.noPhysics
            );
        }

        public boolean shouldEnd() {
            return ticksActive >= ABILITY_DURATION || !player.isAlive() || !isActive;
        }

        public Player getPlayer() {
            return player;
        }
    }
}