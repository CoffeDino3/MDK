package com.CoffeDino.lunacy.abilities;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.effects.AbilityCooldown;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Lunacy.MODID)
public class CelestialAbilityHandler {
    private static final Map<UUID, CelestialAbilityInstance> ACTIVE_ABILITIES = new HashMap<>();
    private static final int MAX_DURATION = 200;
    private static final float GRAVITY_FIELD_RADIUS = 7.5f;
    private static final float GRAVITY_FIELD_HEIGHT = 20f;
    private static final float DAMAGE_PER_SECOND = 0.5f;
    private static final float PUSH_DAMAGE_MULTIPLIER = 2f;
    private static final float PUSH_DAMAGE_FLOOR = 10.0f;
    private static final float PULL_DAMAGE_MULTIPLIER = 1f;
    private static final float PULL_DAMAGE_FLOOR = 6.0f;
    private static final float PUSH_FORCE = 3.0f;
    private static final float PULL_FORCE = 2.5f;
    private static final float SINK_SPEED = 0.3f;
    private static final int SINKING_START_TIME = 140;
    private static final int SINKING_RADIUS = 2;
    private static final int SINKING_DEPTH = 2;
    private static final int COOLDOWN_TICKS = 260;
    private static final int TICK_DAMAGE_PERCENT_UNLOCK_LEVEL = 15;
    private static final float TICK_DAMAGE_MAX_HEALTH_PERCENT = 0.005f;
    private static final float RADIUS_GROWTH_PER_10_LEVELS = 0.5f;
    private static final float PUSH_FORCE_GROWTH_PER_10_LEVELS = 0.2f;
    private static final int SELF_BUFF_UNLOCK_LEVEL = 30;

    public static void activateAbility(Player player) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        if (ACTIVE_ABILITIES.containsKey(playerId) || AbilityCooldown.isActive(player, ModEffects.CELESTIAL_COOLDOWN)) {
            return;
        }

        int level = (player instanceof ServerPlayer sp) ? PlayerLevels.getLevel(sp) : 1;
        ACTIVE_ABILITIES.put(playerId, new CelestialAbilityInstance((ServerPlayer) player, level));
        Lunacy.LOGGER.debug("Celestial gravity ability activated for player: {} (level {})", player.getName().getString(), level);
    }

    public static void deactivateAbility(Player player) {
        ACTIVE_ABILITIES.remove(player.getUUID());
        AbilityCooldown.start(player, ModEffects.CELESTIAL_COOLDOWN, COOLDOWN_TICKS);
        Lunacy.LOGGER.debug("Celestial gravity ability deactivated for player: {}", player.getName().getString());
    }

    public static void performPush(Player player) {
        UUID playerId = player.getUUID();
        CelestialAbilityInstance ability = ACTIVE_ABILITIES.get(playerId);

        if (ability != null) {
            ability.performPush();
            deactivateAbility(player);
        }
    }

    public static void performPull(Player player) {
        UUID playerId = player.getUUID();
        CelestialAbilityInstance ability = ACTIVE_ABILITIES.get(playerId);

        if (ability != null) {
            ability.performPull();
            deactivateAbility(player);
        }
    }

    public static boolean isAbilityActive(Player player) {
        return ACTIVE_ABILITIES.containsKey(player.getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<UUID, CelestialAbilityInstance>> iterator = ACTIVE_ABILITIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, CelestialAbilityInstance> entry = iterator.next();
            CelestialAbilityInstance ability = entry.getValue();

            if (ability.tick()) {
                Lunacy.LOGGER.debug("Celestial ability removed for player {} - duration expired", entry.getKey());
                AbilityCooldown.start(ability.getPlayer(), ModEffects.CELESTIAL_COOLDOWN, COOLDOWN_TICKS);
                iterator.remove();
            }
        }
    }

    private static class CelestialAbilityInstance {
        private final ServerPlayer player;
        private final int level;
        private final float radius;
        private final float pushForce;
        private final float pullForce;
        private final float damageRatioMultiplier;
        private int ticksActive = 0;
        private AABB gravityField;

        public CelestialAbilityInstance(ServerPlayer player, int level) {
            this.player = player;
            this.level = level;

            int tens = level / 10;
            this.radius = GRAVITY_FIELD_RADIUS + tens * RADIUS_GROWTH_PER_10_LEVELS;
            this.pushForce = PUSH_FORCE + tens * PUSH_FORCE_GROWTH_PER_10_LEVELS;
            this.pullForce = PULL_FORCE + tens * (PUSH_FORCE_GROWTH_PER_10_LEVELS * 0.8f);
            this.damageRatioMultiplier = com.CoffeDino.lunacy.races.RaceLevelScaling.genericDamageRatioMultiplier(level);

            updateGravityField();
        }

        public boolean tick() {
            if (!player.isAlive() || player.level().isClientSide()) {
                return true;
            }

            ticksActive++;
            if (ticksActive > MAX_DURATION) {
                return true;
            }

            updateGravityField();
            updateGravityEffects();
            spawnParticles();
            if (ticksActive >= SINKING_START_TIME && ticksActive % 20 == 0) {
                applySinkingBlocks();
            }
            if (level >= SELF_BUFF_UNLOCK_LEVEL && ticksActive % 20 == 0) {
                applySelfBuff();
            }

            return false;
        }

        private void applySelfBuff() {
            int amplifier = Math.min(5, (level - SELF_BUFF_UNLOCK_LEVEL) / 5);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, amplifier, false, true, true));
        }

        private void updateGravityField() {
            Vec3 playerPos = player.position();
            this.gravityField = new AABB(
                    playerPos.x - radius, playerPos.y - GRAVITY_FIELD_HEIGHT, playerPos.z - radius,
                    playerPos.x + radius, playerPos.y + GRAVITY_FIELD_HEIGHT, playerPos.z + radius
            );
        }

        private void updateGravityEffects() {
            ServerLevel level = (ServerLevel) player.level();
            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    gravityField,
                    entity -> entity != player && entity.isAlive()
            );

            for (LivingEntity entity : entities) {
                applyGravityEffects(entity);
                if (ticksActive % 20 == 0) {
                    entity.hurt(player.damageSources().magic(), computeTickDamage(entity));
                }
            }
        }

        private float computeTickDamage(LivingEntity target) {
            float flatDamage = DAMAGE_PER_SECOND * damageRatioMultiplier;
            if (this.level < TICK_DAMAGE_PERCENT_UNLOCK_LEVEL) {
                return flatDamage;
            }
            float percentDamage = target.getMaxHealth() * TICK_DAMAGE_MAX_HEALTH_PERCENT * damageRatioMultiplier;
            return Math.max(flatDamage, percentDamage);
        }

        private void applyGravityEffects(LivingEntity entity) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 4));
            if (entity instanceof Mob mob) {
                mob.setNoGravity(false);
            }
            if (entity.level().isEmptyBlock(entity.blockPosition().below())) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, -SINK_SPEED, 0));
                entity.hurtMarked = true;
            }
            Vec3 currentMotion = entity.getDeltaMovement();
            entity.setDeltaMovement(currentMotion.x * 0.7, currentMotion.y, currentMotion.z * 0.7);
        }

        private void applySinkingBlocks() {
            ServerLevel level = (ServerLevel) player.level();
            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    gravityField,
                    entity -> entity != player && entity.isAlive()
            );

            for (LivingEntity entity : entities) {
                breakBlocksUnderEntity(level, entity);
            }

        }

        private void breakBlocksUnderEntity(ServerLevel level, LivingEntity entity) {
            BlockPos entityPos = entity.blockPosition();
            for (int x = -SINKING_RADIUS; x <= SINKING_RADIUS; x++) {
                for (int z = -SINKING_RADIUS; z <= SINKING_RADIUS; z++) {
                    for (int y = 0; y > -SINKING_DEPTH; y--) {
                        BlockPos breakPos = entityPos.offset(x, y, z);
                        BlockState blockState = level.getBlockState(breakPos);
                        if (canBreakBlock(blockState, level, breakPos)) {
                            level.destroyBlock(breakPos, true, player);
                            level.sendParticles(
                                    new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                                    breakPos.getX() + 0.5, breakPos.getY() + 0.5, breakPos.getZ() + 0.5,
                                    10, 0.3, 0.3, 0.3, 0.02
                            );
                        }
                    }
                }
            }
        }

        private boolean canBreakBlock(BlockState blockState, ServerLevel level, BlockPos pos) {
            if (blockState.isAir() ||
                    !blockState.getFluidState().isEmpty() ||
                    blockState.getDestroySpeed(level, pos) < 0) {
                return false;
            }

            return true;
        }

        private void spawnParticles() {
            ServerLevel level = (ServerLevel) player.level();
            for (int i = 0; i < 60; i++) {
                double x = gravityField.minX + Math.random() * (gravityField.maxX - gravityField.minX);
                double y = gravityField.minY + Math.random() * (gravityField.maxY - gravityField.minY);
                double z = gravityField.minZ + Math.random() * (gravityField.maxZ - gravityField.minZ);

                level.sendParticles(
                        new DustParticleOptions(new Vector3f(0.5f, 0.2f, 0.8f), 1.0f),
                        x, y, z, 1, 0, -0.1, 0, 0.05
                );
            }
        }

        public void performPush() {
            ServerLevel level = (ServerLevel) player.level();
            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    gravityField,
                    entity -> entity != player && entity.isAlive()
            );

            float mainHandDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float pushDamage = Math.max(PUSH_DAMAGE_FLOOR, mainHandDamage * PUSH_DAMAGE_MULTIPLIER) * damageRatioMultiplier;

            for (LivingEntity entity : entities) {
                Vec3 direction = entity.position().subtract(player.position()).normalize();
                Vec3 pushVec = direction.scale(pushForce);
                entity.setDeltaMovement(pushVec.x, pushVec.y + 0.5, pushVec.z);
                entity.hurtMarked = true;
                entity.hurt(player.damageSources().magic(), pushDamage);
                level.sendParticles(
                        ParticleTypes.SWEEP_ATTACK,
                        entity.getX(), entity.getY() + 1, entity.getZ(),
                        5, 0.5, 0.5, 0.5, 0.1
                );
            }

            spawnBurstParticles(level);
        }

        public void performPull() {
            ServerLevel level = (ServerLevel) player.level();
            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    gravityField,
                    entity -> entity != player && entity.isAlive()
            );

            float mainHandDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float pullDamage = Math.max(PULL_DAMAGE_FLOOR, mainHandDamage * PULL_DAMAGE_MULTIPLIER) * damageRatioMultiplier;

            for (LivingEntity entity : entities) {
                Vec3 toPlayer = player.position().subtract(entity.position());
                double distance = toPlayer.length();

                if (distance > 1.0) {
                    Vec3 gentlePull = toPlayer.normalize().scale(pullForce - PULL_FORCE + 2.4);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(gentlePull));
                }
                entity.hurtMarked = true;
                entity.hurt(player.damageSources().magic(), pullDamage);
                level.sendParticles(
                        ParticleTypes.DRAGON_BREATH,
                        entity.getX(), entity.getY() + 1, entity.getZ(),
                        5, 0.3, 0.3, 0.3, 0.05
                );
            }

            spawnImplosionParticles(level);
        }

        private void spawnBurstParticles(ServerLevel level) {
            Vec3 center = gravityField.getCenter();
            for (int i = 0; i < 50; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double distance = Math.random() * radius;
                double x = center.x + Math.cos(angle) * distance;
                double z = center.z + Math.sin(angle) * distance;
                double y = center.y + (Math.random() - 0.5) * GRAVITY_FIELD_HEIGHT;

                level.sendParticles(
                        ParticleTypes.PORTAL,
                        x, y, z, 1, 0.1, 0.1, 0.1, 0.02
                );
            }
        }

        private void spawnImplosionParticles(ServerLevel level) {
            Vec3 center = player.position();
            for (int i = 0; i < 50; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double distance = radius * (1 - Math.random() * 0.5);
                double x = center.x + Math.cos(angle) * distance;
                double z = center.z + Math.sin(angle) * distance;
                double y = center.y + (Math.random() - 0.5) * GRAVITY_FIELD_HEIGHT;

                Vec3 particlePos = new Vec3(x, y, z);
                Vec3 direction = center.subtract(particlePos).normalize().scale(0.1);

                level.sendParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        x, y, z, 1, direction.x, direction.y, direction.z, 0.02
                );
            }
        }

        public ServerPlayer getPlayer() {
            return player;
        }
    }
}
