package com.CoffeDino.lunacy.abilities;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.effects.AbilityCooldown;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = Lunacy.MODID)
public class WarderAbilityHandler {
    private static final Map<UUID, WarderAbilityInstance> ACTIVE_ABILITIES = new HashMap<>();
    private static final int ABILITY_DURATION = 100;
    private static final float DAMAGE_MULTIPLIER = 1.5f;
    private static final float DAMAGE_FLOOR = 8.0f;
    private static final float BASE_CIRCLE_RADIUS = 2.0f;
    private static final float CIRCLE_DISTANCE = 2.5f;
    private static final int COOLDOWN_TICKS = 60;
    private static final int RESIZE_UNLOCK_LEVEL = 15;
    private static final float MIN_RADIUS = 0.5f;
    private static final float MAX_RADIUS_GROWTH_PER_5_LEVELS = 0.5f;
    private static final float SCROLL_STEP = 0.25f;

    public static void activateAbility(Player player) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        if (ACTIVE_ABILITIES.containsKey(playerId) || AbilityCooldown.isActive(player, ModEffects.WARDER_COOLDOWN)) {
            return;
        }

        int level = (player instanceof ServerPlayer sp) ? PlayerLevels.getLevel(sp) : 1;
        ACTIVE_ABILITIES.put(playerId, new WarderAbilityInstance(player, level));
        Lunacy.LOGGER.debug("Warder ability activated for player: {}", player.getName().getString());
    }

    private static float getMaxRadius(int level) {
        if (level < RESIZE_UNLOCK_LEVEL) return BASE_CIRCLE_RADIUS;
        int tiers = ((level - RESIZE_UNLOCK_LEVEL) / 5) + 1;
        return BASE_CIRCLE_RADIUS + tiers * MAX_RADIUS_GROWTH_PER_5_LEVELS;
    }
    public static void adjustRingSize(ServerPlayer player, boolean scrollUp) {
        WarderAbilityInstance ability = ACTIVE_ABILITIES.get(player.getUUID());
        if (ability == null) return;
        if (ability.level < RESIZE_UNLOCK_LEVEL) return;

        float maxRadius = getMaxRadius(ability.level);
        float newRadius = ability.currentRadius + (scrollUp ? SCROLL_STEP : -SCROLL_STEP);
        ability.currentRadius = Math.max(MIN_RADIUS, Math.min(maxRadius, newRadius));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<UUID, WarderAbilityInstance>> iterator = ACTIVE_ABILITIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, WarderAbilityInstance> entry = iterator.next();
            WarderAbilityInstance ability = entry.getValue();

            if (ability.tick() || !ability.isValid()) {
                iterator.remove();
                AbilityCooldown.start(ability.getPlayer(), ModEffects.WARDER_COOLDOWN, COOLDOWN_TICKS);
                Lunacy.LOGGER.debug("Warder ability ended for player: {}", ability.getPlayer().getName().getString());
            }
        }
    }

    public static boolean isAbilityActive(Player player) {
        return ACTIVE_ABILITIES.containsKey(player.getUUID());
    }

    public static void deactivateAbility(Player player) {
        ACTIVE_ABILITIES.remove(player.getUUID());
    }

    private static class WarderAbilityInstance {
        private final ServerPlayer player;
        private final int level;
        private float currentRadius;
        private int ticksActive = 0;
        private final Set<BlockPos> brokenBlocks = new HashSet<>();

        public WarderAbilityInstance(Player player, int level) {
            this.player = (ServerPlayer) player;
            this.level = level;
            this.currentRadius = BASE_CIRCLE_RADIUS;
        }

        public boolean tick() {
            if (!player.isAlive() || player.level().isClientSide()) {
                return true;
            }

            ticksActive++;
            if (ticksActive > ABILITY_DURATION) {
                return true;
            }

            Vec3 lookVec;
            Vec3 circleCenter;
            if (player.isShiftKeyDown()) {
                lookVec = new Vec3(0, -1, 0);
                circleCenter = player.position().add(0, -0.3, 0);
            } else {
                lookVec = player.getLookAngle().normalize();
                Vec3 playerPos = player.position().add(0, player.getEyeHeight() * 0.7, 0);
                circleCenter = playerPos.add(lookVec.x * CIRCLE_DISTANCE, lookVec.y * CIRCLE_DISTANCE + 0.7, lookVec.z * CIRCLE_DISTANCE);
            }
            spawnParticles(circleCenter, lookVec);
            damageEntities(circleCenter, lookVec);
            breakBlocks(circleCenter, lookVec);

            return false;
        }

        private void spawnParticles(Vec3 center, Vec3 lookVec) {
            ServerLevel level = (ServerLevel) player.level();
            int particles = 24;
            Vec3 up = new Vec3(0, 1, 0);
            Vec3 right = lookVec.cross(up).normalize();

            if (right.length() < 0.1) {
                right = new Vec3(1, 0, 0);
            }

            Vec3 actualUp = right.cross(lookVec).normalize();

            for (int i = 0; i < particles; i++) {
                double angle = 2 * Math.PI * i / particles;
                double xOffset = currentRadius * Math.cos(angle);
                double yOffset = currentRadius * Math.sin(angle);

                Vec3 particlePos = center.add(
                        right.x * xOffset + actualUp.x * yOffset,
                        right.y * xOffset + actualUp.y * yOffset,
                        right.z * xOffset + actualUp.z * yOffset
                );
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0.05);

                if (i % 2 == 0) {
                    double innerRadius = currentRadius * 0.7;
                    double innerX = innerRadius * Math.cos(angle);
                    double innerY = innerRadius * Math.sin(angle);

                    Vec3 innerPos = center.add(
                            right.x * innerX + actualUp.x * innerY,
                            right.y * innerX + actualUp.y * innerY,
                            right.z * innerX + actualUp.z * innerY
                    );

                    level.sendParticles(ParticleTypes.GLOW,
                            innerPos.x, innerPos.y, innerPos.z, 1, 0, 0, 0, 0.03);
                }
            }
        }

        private void damageEntities(Vec3 center, Vec3 lookVec) {
            Vec3 up = new Vec3(0, 1, 0);
            Vec3 right = lookVec.cross(up).normalize();
            if (right.length() < 0.1) right = new Vec3(1, 0, 0);
            Vec3 actualUp = right.cross(lookVec).normalize();

            Vec3 min = center.subtract(right.scale(currentRadius)).subtract(actualUp.scale(currentRadius));
            Vec3 max = center.add(right.scale(currentRadius)).add(actualUp.scale(currentRadius));

            Vec3 thicknessVec = lookVec.scale(0.5);
            AABB damageArea = new AABB(
                    min.subtract(thicknessVec),
                    max.add(thicknessVec)
            );

            List<LivingEntity> entities = player.level().getEntitiesOfClass(
                    LivingEntity.class, damageArea, entity ->
                            entity != player && entity.isAlive()
            );

            for (LivingEntity entity : entities) {
                if (ticksActive % 10 == 0) {
                    Vec3 toEntity = entity.position().subtract(center);
                    double distanceInPlane = toEntity.subtract(lookVec.scale(toEntity.dot(lookVec))).length();

                    if (distanceInPlane <= currentRadius) {
                        float mainHandDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        float ringDamage = Math.max(DAMAGE_FLOOR, mainHandDamage * DAMAGE_MULTIPLIER);

                        entity.hurt(player.damageSources().playerAttack(player), ringDamage);
                        Vec3 knockback = toEntity.normalize().scale(0.3);
                        entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
                    }
                }
            }
        }

        private void breakBlocks(Vec3 center, Vec3 lookVec) {
            ServerLevel level = (ServerLevel) player.level();
            Vec3 up = new Vec3(0, 1, 0);
            Vec3 right = lookVec.cross(up).normalize();
            if (right.length() < 0.1) right = new Vec3(1, 0, 0);
            Vec3 actualUp = right.cross(lookVec).normalize();
            int gridSize = (int) (currentRadius * 2) + 1;
            for (int u = -gridSize; u <= gridSize; u++) {
                for (int v = -gridSize; v <= gridSize; v++) {
                    double distance = Math.sqrt(u * u + v * v);
                    if (distance <= currentRadius) {
                        Vec3 blockOffset = right.scale(u).add(actualUp.scale(v));
                        BlockPos pos = new BlockPos(
                                (int) Math.floor(center.x + blockOffset.x),
                                (int) Math.floor(center.y + blockOffset.y),
                                (int) Math.floor(center.z + blockOffset.z)
                        );

                        if (!brokenBlocks.contains(pos)) {
                            breakBlockAt(pos, level);
                        }
                    }
                }
            }
        }

        private void breakBlockAt(BlockPos pos, ServerLevel level) {
            BlockState state = level.getBlockState(pos);

            if (state.isAir()) {
                return;
            }

            if (state.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) {
                level.destroyBlock(pos, false, player);
                brokenBlocks.add(pos);

                level.sendParticles(ParticleTypes.LARGE_SMOKE,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        4, 0.2, 0.2, 0.2, 0.02);
                return;
            }

            if (state.is(net.minecraft.world.level.block.Blocks.BEDROCK) || isMineral(state)) {
                return;
            }

            float destroySpeed = state.getDestroySpeed(level, pos);
            if (destroySpeed < 0) {
                return;
            }

            if (destroySpeed < 50.0f) {
                level.destroyBlock(pos, true, player);
                brokenBlocks.add(pos);

                level.sendParticles(ParticleTypes.CRIT,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        3, 0.2, 0.2, 0.2, 0.05);
            }
        }

        private boolean isMineral(BlockState state) {
            if (state.is(net.minecraft.tags.BlockTags.COAL_ORES) ||
                    state.is(net.minecraft.tags.BlockTags.IRON_ORES) ||
                    state.is(net.minecraft.tags.BlockTags.COPPER_ORES) ||
                    state.is(net.minecraft.tags.BlockTags.GOLD_ORES) ||
                    state.is(net.minecraft.tags.BlockTags.REDSTONE_ORES) ||
                    state.is(net.minecraft.tags.BlockTags.LAPIS_ORES) ||
                    state.is(net.minecraft.tags.BlockTags.DIAMOND_ORES) ||
                    state.is(net.minecraft.tags.BlockTags.EMERALD_ORES)) {
                return true;
            }
            net.minecraft.resources.ResourceLocation id =
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (id == null) return false;
            String path = id.getPath();
            return path.contains("_ore") || path.equals("ancient_debris") || path.startsWith("raw_");
        }

        public boolean isValid() {
            return player != null && player.isAlive() && !player.isRemoved();
        }

        public ServerPlayer getPlayer() {
            return player;
        }
    }
}
