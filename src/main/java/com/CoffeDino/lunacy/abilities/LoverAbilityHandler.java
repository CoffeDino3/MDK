package com.CoffeDino.lunacy.abilities;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.effects.AbilityCooldown;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;


import java.util.*;

@EventBusSubscriber(modid = Lunacy.MODID)
public class LoverAbilityHandler {
    private static final Map<UUID, LoverAbilityInstance> ACTIVE_ABILITIES = new HashMap<>();
    private static final int BASE_ABILITY_DURATION = 400;
    private static final float DURATION_GROWTH_PER_10_LEVELS = 0.10f;
    private static final float CIRCLE_RADIUS = 1.0f;
    private static final int COOLDOWN_TICKS = 280;
    private static final float DAMAGE_RATIO_GROWTH_PER_LEVEL = 0.02f;

    private static int getDuration(int level) {
        float multiplier = 1.0f + (level / 10) * DURATION_GROWTH_PER_10_LEVELS;
        return Math.round(BASE_ABILITY_DURATION * multiplier);
    }

    private static float getDamageRatio(int level) {
        return 1.0f + level * DAMAGE_RATIO_GROWTH_PER_LEVEL;
    }

    public static void activateAbility(Player player) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        if (ACTIVE_ABILITIES.containsKey(playerId) || AbilityCooldown.isActive(player, ModEffects.LOVER_COOLDOWN)) {
            return;
        }

        int level = (player instanceof ServerPlayer sp) ? PlayerLevels.getLevel(sp) : 1;
        ACTIVE_ABILITIES.put(playerId, new LoverAbilityInstance(player, level));
        Lunacy.LOGGER.debug("Lover ability activated for player: {} (level {})", player.getName().getString(), level);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<UUID, LoverAbilityInstance>> iterator = ACTIVE_ABILITIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, LoverAbilityInstance> entry = iterator.next();
            LoverAbilityInstance ability = entry.getValue();

            if (ability.tick()) {
                UUID playerId = entry.getKey();
                iterator.remove();
                AbilityCooldown.start(ability.getPlayer(), ModEffects.LOVER_COOLDOWN, COOLDOWN_TICKS);
                Lunacy.LOGGER.debug("Lover ability removed for player {} - tick() returned true", playerId);
            } else if (!ability.isValid()) {
                UUID playerId = entry.getKey();
                iterator.remove();
                AbilityCooldown.start(ability.getPlayer(), ModEffects.LOVER_COOLDOWN, COOLDOWN_TICKS);
                Lunacy.LOGGER.debug("Lover ability removed for player {} - invalid", playerId);
            }
        }
    }



    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LoverAbilityInstance ability = ACTIVE_ABILITIES.get(player.getUUID());
            if (ability != null) {
                DamageSource source = event.getSource();
                float damageAmount = event.getAmount();
                redirectDamageToRandomEntity(player, damageAmount, source, ability.damageRatio);
                event.setCanceled(true);
            }
        }
    }

    private static void redirectDamageToRandomEntity(ServerPlayer player, float damageAmount, DamageSource originalSource, float damageRatio) {
        ServerLevel level = (ServerLevel) player.level();
        Vec3 playerPos = player.position();
        AABB searchArea30 = new AABB(
                playerPos.x - 30, playerPos.y - 10, playerPos.z - 30,
                playerPos.x + 30, playerPos.y + 10, playerPos.z + 30
        );

        List<LivingEntity> entitiesIn30 = level.getEntitiesOfClass(
                LivingEntity.class, searchArea30, entity ->
                        entity != player && entity.isAlive() && !entity.isAlliedTo(player)
        );

        LivingEntity target = null;

        if (!entitiesIn30.isEmpty()) {
            Collections.shuffle(entitiesIn30);
            target = entitiesIn30.get(0);
        } else {
            AABB searchArea1000 = new AABB(
                    playerPos.x - 1000, playerPos.y - 50, playerPos.z - 1000,
                    playerPos.x + 1000, playerPos.y + 50, playerPos.z + 1000
            );

            List<LivingEntity> entitiesIn1000 = level.getEntitiesOfClass(
                    LivingEntity.class, searchArea1000, entity ->
                            entity != player && entity.isAlive() && !entity.isAlliedTo(player)
            );

            if (!entitiesIn1000.isEmpty()) {
                target = entitiesIn1000.stream()
                        .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(playerPos)))
                        .orElse(null);

                if (target != null) {
                    Lunacy.LOGGER.debug("Found nearest target in 1000-block radius: {} at {} blocks",
                            target.getName().getString(), Math.sqrt(target.distanceToSqr(playerPos)));
                }
            }
        }

        if (target != null) {
            target.hurt(player.damageSources().magic(), damageAmount * damageRatio);

            level.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                    target.getX(), target.getY() + 1, target.getZ(),
                    15, 0.5, 0.8, 0.5, 0.1);

            spawnDamageTransferParticles(player.position(), target.position(), level);


        } else {
            Lunacy.LOGGER.debug("No valid target found for damage redirection - damage absorbed");

            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    player.getX(), player.getY() + 1, player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.05);
        }
    }

    private static void spawnDamageTransferParticles(Vec3 start, Vec3 end, ServerLevel level) {
        int particles = 20;
        Vec3 direction = end.subtract(start);
        Vec3 step = direction.scale(1.0 / particles);

        for (int i = 0; i < particles; i++) {
            Vec3 particlePos = start.add(step.scale(i));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, 0.1, 0.1, 0.1, 0.02);
            if (i % 5 == 0) {
                level.sendParticles(ParticleTypes.HEART,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }

    public static boolean isAbilityActive(Player player) {
        return ACTIVE_ABILITIES.containsKey(player.getUUID());
    }

    public static void deactivateAbility(Player player) {
        ACTIVE_ABILITIES.remove(player.getUUID());
    }

    private static class LoverAbilityInstance {
        private final ServerPlayer player;
        private final int abilityDuration;
        private final float damageRatio;
        private int ticksActive = 0;

        public LoverAbilityInstance(Player player, int level) {
            this.player = (ServerPlayer) player;
            this.abilityDuration = getDuration(level);
            this.damageRatio = getDamageRatio(level);
        }

        public boolean tick() {
            if (!player.isAlive() || player.level().isClientSide()) {
                return true;
            }

            ticksActive++;
            if (ticksActive > abilityDuration) {
                return true;
            }

            Vec3 playerPos = player.position().add(0, 0.5, 0);
            spawnParticles(playerPos);

            return false;
        }

        private void spawnParticles(Vec3 center) {
            ServerLevel level = (ServerLevel) player.level();
            int particles = 16;
            double[] heights = {-0.3, -0.1, 0.1, 0.3, 0.5, 0.7, 0.9, 1.1, 1.3, 1.5};

            for (double height : heights) {
                for (int i = 0; i < particles; i++) {
                    double angle = 2 * Math.PI * i / particles;
                    double x = center.x + CIRCLE_RADIUS * Math.cos(angle);
                    double z = center.z + CIRCLE_RADIUS * Math.sin(angle);

                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            x, center.y + height, z, 1, 0, 0, 0, 0);
                }
            }
        }

        public boolean isValid() {
            return player != null && player.isAlive() && !player.isRemoved();
        }

        public ServerPlayer getPlayer() {
            return player;
        }
    }
}
