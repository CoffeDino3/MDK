package com.CoffeDino.lunacy.abilities;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.effects.AbilityCooldown;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Lunacy.MODID)
public class EtherealAbilityHandler {
    private static final Map<UUID, EtherealAbilityInstance> ACTIVE_ABILITIES = new HashMap<>();
    private static final Map<UUID, Boolean> PLAYER_JUMPING = new HashMap<>();
    private static final Map<UUID, Boolean> PLAYER_SHIFTING = new HashMap<>();
    private static final int COOLDOWN_TICKS = 600;
    private static final int BASE_ABILITY_DURATION = 200;
    private static final float DURATION_GROWTH_PER_10_LEVELS = 0.50f;
    private static final float MOVE_SPEED = 0.1f;
    private static final float VERTICAL_SPEED = 0.2f;
    private static final ResourceLocation NO_ENTITY_INTERACT_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "ethereal_no_entity_interact");

    private static int getDuration(int level) {
        float multiplier = 1.0f + (level / 10) * DURATION_GROWTH_PER_10_LEVELS;
        return Math.round(BASE_ABILITY_DURATION * multiplier);
    }

    public static void activateAbility(Player player, boolean jumping, boolean shifting) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        if (ACTIVE_ABILITIES.containsKey(playerId) || AbilityCooldown.isActive(player, ModEffects.ETHEREAL_COOLDOWN)) {
            return;
        }
        PLAYER_JUMPING.put(playerId, jumping);
        PLAYER_SHIFTING.put(playerId, shifting);

        int level = (player instanceof ServerPlayer sp) ? PlayerLevels.getLevel(sp) : 1;
        EtherealAbilityInstance ability = new EtherealAbilityInstance((ServerPlayer) player, getDuration(level), level);
        ACTIVE_ABILITIES.put(playerId, ability);

        Lunacy.LOGGER.debug("Ethereal ability activated for player: {} (duration {})", player.getName().getString(), ability.abilityDuration);
    }

    public static void updateEtherealInput(ServerPlayer player, boolean jumping, boolean shifting) {
        if (isAbilityActive(player)) {
            UUID playerId = player.getUUID();
            PLAYER_JUMPING.put(playerId, jumping);
            PLAYER_SHIFTING.put(playerId, shifting);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ACTIVE_ABILITIES.values().forEach(EtherealAbilityInstance::tick);
        ACTIVE_ABILITIES.entrySet().removeIf(entry -> {
            EtherealAbilityInstance ability = entry.getValue();
            if (ability.shouldEnd()) {
                ability.forceEnd();
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
            ability.forceEnd();
        }
    }

    public static boolean canPhaseThroughBlocks(Player player) {
        return isAbilityActive(player);
    }

    private static class EtherealAbilityInstance {
        private final ServerPlayer player;
        private final ServerLevel level;
        private final int abilityDuration;
        private final int playerLevel;
        private int ticksActive = 0;
        private boolean isActive = false;
        private Vec3 safePosition;
        private boolean wasInBlock = false;
        private GameType originalGameType;
        private boolean wasFlyingBeforeAbility;
        private Vec3 lastValidPosition;
        private static final int AURA_MIN_LEVEL = 25;
        private static final int AURA_INTERVAL_TICKS = 30;
        private static final double AURA_RADIUS = 5.0;
        private static final int PARTICLE_INTERVAL_TICKS = 2;
        private static final int PARTICLES_PER_BURST = 3;
        private static final double PARTICLE_SPAWN_RADIUS = 1.1;
        private static final Vector3f AURA_COLOR_PINK = new Vector3f(1.0f, 0.55f, 0.8f);
        private static final Vector3f AURA_COLOR_WHITE = new Vector3f(1.0f, 1.0f, 1.0f);

        public EtherealAbilityInstance(ServerPlayer player, int abilityDuration, int playerLevel) {
            this.player = player;
            this.level = (ServerLevel) player.level();
            this.abilityDuration = abilityDuration;
            this.playerLevel = playerLevel;
            this.safePosition = player.position();
            this.lastValidPosition = player.position();
            activate();
        }

        public void activate() {
            if (!player.isAlive()) return;
            this.safePosition = player.position();
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, abilityDuration, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, abilityDuration, 0, false, false));
            this.originalGameType = player.gameMode.getGameModeForPlayer();
            this.wasFlyingBeforeAbility = player.getAbilities().flying;
            player.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
            AttributeInstance entityRange = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (entityRange != null) {
                entityRange.addOrUpdateTransientModifier(new AttributeModifier(
                        NO_ENTITY_INTERACT_MODIFIER_ID,
                        -100.0,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }

            this.isActive = true;

            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Ethereal Form activated! You can phase through blocks for " + (abilityDuration / 20) + " seconds."),
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

            enforceNoMobEntry();

            if (ticksActive % PARTICLE_INTERVAL_TICKS == 0) {
                spawnAuraParticles();
            }

            if (playerLevel >= AURA_MIN_LEVEL && ticksActive % AURA_INTERVAL_TICKS == 0) {
                dealAuraDamage();
            }

            if (ticksActive >= abilityDuration) {
                forceEnd();
            }
        }

        private void spawnAuraParticles() {
            for (int i = 0; i < PARTICLES_PER_BURST; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2;
                double radius = PARTICLE_SPAWN_RADIUS * (0.4 + level.random.nextDouble() * 0.6);
                double x = player.getX() + Math.cos(angle) * radius;
                double z = player.getZ() + Math.sin(angle) * radius;
                double y = player.getY() + level.random.nextDouble() * player.getBbHeight();

                Vector3f color = level.random.nextBoolean() ? AURA_COLOR_PINK : AURA_COLOR_WHITE;
                DustParticleOptions options = new DustParticleOptions(color, 1.0f);
                level.sendParticles(options, x, y, z, 1, 0, 0.01, 0, 0.0);
            }
        }

        private void dealAuraDamage() {
            float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            AABB auraBox = player.getBoundingBox().inflate(AURA_RADIUS);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, auraBox,
                    e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().indirectMagic(player, player), damage);
            }
        }
        private void enforceNoMobEntry() {
            boolean overlappingMob = !level.getEntities(player, player.getBoundingBox(),
                    e -> e instanceof LivingEntity && e != player).isEmpty();

            if (overlappingMob) {
                player.teleportTo(lastValidPosition.x, lastValidPosition.y, lastValidPosition.z);
                player.setDeltaMovement(Vec3.ZERO);
            } else {
                lastValidPosition = player.position();
            }
        }

        public void forceEnd() {
            Vec3 safeExit = findNearestSafePosition();
            deactivate();

            if (safeExit != null) {
                player.teleportTo(safeExit.x, safeExit.y, safeExit.z);
            } else {
                emergencyEscapeFromBlocks();
            }
            player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            ensureNotStuckInEntity();
        }
        private void ensureNotStuckInEntity() {
            for (int attempt = 0; attempt < 10; attempt++) {
                AABB playerBox = player.getBoundingBox();
                boolean overlapping = !level.getEntities(player, playerBox,
                        e -> e instanceof LivingEntity && e != player).isEmpty();
                if (!overlapping) return;
                player.teleportTo(player.getX(), player.getY() + 1.0, player.getZ());
            }
        }

        public void deactivate() {
            if (!isActive) return;
            AbilityCooldown.start(player, ModEffects.ETHEREAL_COOLDOWN, COOLDOWN_TICKS);
            player.removeEffect(MobEffects.GLOWING);
            player.removeEffect(MobEffects.INVISIBILITY);
            player.gameMode.changeGameModeForPlayer(originalGameType);
            player.getAbilities().flying = player.getAbilities().mayfly && wasFlyingBeforeAbility;
            player.onUpdateAbilities();
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;

            AttributeInstance entityRange = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (entityRange != null) {
                entityRange.removeModifier(NO_ENTITY_INTERACT_MODIFIER_ID);
            }

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
            boolean blocksClear = level.isEmptyBlock(pos) &&
                    level.isEmptyBlock(pos.above()) &&
                    level.isEmptyBlock(pos.above(2)) &&
                    !level.isEmptyBlock(pos.below());
            if (!blocksClear) return false;
            AABB entityCheckBox = new AABB(pos).inflate(0.15, 0, 0.15).expandTowards(0, 1.8, 0);
            return level.getEntities(player, entityCheckBox,
                    e -> e instanceof LivingEntity && e != player).isEmpty();
        }

        public boolean shouldEnd() {
            return ticksActive >= abilityDuration || !player.isAlive() || !isActive;
        }

        public ServerPlayer getPlayer() {
            return player;
        }
    }
}