package net.CoffeDino.testmod.abilities;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID)
public class WarderAbilityHandler {
    private static final Map<UUID, WarderAbilityInstance> ACTIVE_ABILITIES = new HashMap<>();
    private static final int ABILITY_DURATION = 100;
    private static final float DAMAGE = 8.0f;
    private static final float CIRCLE_RADIUS = 2.0f;
    private static final float CIRCLE_DISTANCE = 3.0f;

    public static void activateAbility(Player player) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        if (ACTIVE_ABILITIES.containsKey(playerId)) {
            return;
        }

        ACTIVE_ABILITIES.put(playerId, new WarderAbilityInstance(player));
        TestingCoffeDinoMod.LOGGER.debug("Warder ability activated for player: {}", player.getName().getString());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<Map.Entry<UUID, WarderAbilityInstance>> iterator = ACTIVE_ABILITIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, WarderAbilityInstance> entry = iterator.next();
            WarderAbilityInstance ability = entry.getValue();

            if (ability.tick() || !ability.isValid()) {
                iterator.remove();
                TestingCoffeDinoMod.LOGGER.debug("Warder ability ended for player: {}", ability.getPlayer().getName().getString());
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
        private int ticksActive = 0;
        private final Set<BlockPos> brokenBlocks = new HashSet<>();

        public WarderAbilityInstance(Player player) {
            this.player = (ServerPlayer) player;
        }

        public boolean tick() {
            if (!player.isAlive() || player.level().isClientSide()) {
                return true;
            }

            ticksActive++;
            if (ticksActive > ABILITY_DURATION) {
                return true;
            }
            Vec3 lookVec = player.getLookAngle().normalize();
            Vec3 playerPos = player.position().add(0, player.getEyeHeight() * 0.7, 0);
            Vec3 circleCenter = playerPos.add(lookVec.x * CIRCLE_DISTANCE, lookVec.y * CIRCLE_DISTANCE+0.7, lookVec.z * CIRCLE_DISTANCE);
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
                double xOffset = CIRCLE_RADIUS * Math.cos(angle);
                double yOffset = CIRCLE_RADIUS * Math.sin(angle);

                Vec3 particlePos = center.add(
                        right.x * xOffset + actualUp.x * yOffset,
                        right.y * xOffset + actualUp.y * yOffset,
                        right.z * xOffset + actualUp.z * yOffset
                );
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0.05);

                if (i % 2 == 0) {
                    double innerRadius = CIRCLE_RADIUS * 0.7;
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

            Vec3 min = center.subtract(right.scale(CIRCLE_RADIUS)).subtract(actualUp.scale(CIRCLE_RADIUS));
            Vec3 max = center.add(right.scale(CIRCLE_RADIUS)).add(actualUp.scale(CIRCLE_RADIUS));

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
                if (ticksActive % 10 == 0) { // Damage every 0.5 seconds
                    Vec3 toEntity = entity.position().subtract(center);
                    double distanceInPlane = toEntity.subtract(lookVec.scale(toEntity.dot(lookVec))).length();

                    if (distanceInPlane <= CIRCLE_RADIUS) {
                        entity.hurt(player.damageSources().playerAttack(player), DAMAGE);
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
            int gridSize = (int) (CIRCLE_RADIUS * 2) + 1;
            for (int u = -gridSize; u <= gridSize; u++) {
                for (int v = -gridSize; v <= gridSize; v++) {
                    double distance = Math.sqrt(u * u + v * v);
                    if (distance <= CIRCLE_RADIUS) {
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

            if (state.isAir() || state.getDestroySpeed(level, pos) < 0) {
                return;
            }

            float destroySpeed = state.getDestroySpeed(level, pos);
            if (destroySpeed < 50.0f) {
                level.destroyBlock(pos, true, player);
                brokenBlocks.add(pos);

                level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        3, 0.2, 0.2, 0.2, 0.05);
            }
        }

        public boolean isValid() {
            return player != null && player.isAlive() && !player.isRemoved();
        }

        public Player getPlayer() {
            return player;
        }
    }

    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_DURATION = 3000;

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
}