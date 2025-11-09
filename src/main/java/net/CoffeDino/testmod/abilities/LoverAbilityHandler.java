package net.CoffeDino.testmod.abilities;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID)
public class LoverAbilityHandler {
    private static final Map<UUID, LoverAbilityInstance> ACTIVE_ABILITIES = new HashMap<>();
    private static final int ABILITY_DURATION = 400;
    private static final float CIRCLE_RADIUS = 1.0f;
    private static final long COOLDOWN_DURATION = 14000;

    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    public static void activateAbility(Player player) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        if (ACTIVE_ABILITIES.containsKey(playerId) || !canActivateAbility(player)) {
            return;
        }

        ACTIVE_ABILITIES.put(playerId, new LoverAbilityInstance(player));
        startCooldown(player);
        TestingCoffeDinoMod.LOGGER.debug("Lover ability activated for player: {}", player.getName().getString());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<Map.Entry<UUID, LoverAbilityInstance>> iterator = ACTIVE_ABILITIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, LoverAbilityInstance> entry = iterator.next();
            LoverAbilityInstance ability = entry.getValue();

            if (ability.tick()) {
                UUID playerId = entry.getKey();
                iterator.remove();
                TestingCoffeDinoMod.LOGGER.debug("Lover ability removed for player {} - tick() returned true", playerId);
            } else if (!ability.isValid()) {
                UUID playerId = entry.getKey();
                iterator.remove();
                TestingCoffeDinoMod.LOGGER.debug("Lover ability removed for player {} - invalid", playerId);
            }
        }
    }



    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (isAbilityActive(player)) {
                DamageSource source = event.getSource();
                float damageAmount = event.getAmount();
                redirectDamageToRandomEntity(player, damageAmount, source);
                event.setCanceled(true);

                TestingCoffeDinoMod.LOGGER.debug("Lover ability redirected {} damage from source: {}",
                        damageAmount, source.getMsgId());
            }
        }
    }

    private static void redirectDamageToRandomEntity(ServerPlayer player, float damageAmount, DamageSource originalSource) {
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
            TestingCoffeDinoMod.LOGGER.debug("Found random target in 30-block radius: {}", target.getName().getString());
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
                    TestingCoffeDinoMod.LOGGER.debug("Found nearest target in 1000-block radius: {} at {} blocks",
                            target.getName().getString(), Math.sqrt(target.distanceToSqr(playerPos)));
                }
            }
        }

        if (target != null) {
            target.hurt(player.damageSources().magic(), damageAmount);

            level.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                    target.getX(), target.getY() + 1, target.getZ(),
                    15, 0.5, 0.8, 0.5, 0.1);

            spawnDamageTransferParticles(player.position(), target.position(), level);

            TestingCoffeDinoMod.LOGGER.debug("Redirected {} damage from {} to {}",
                    damageAmount, player.getName().getString(), target.getName().getString());
        } else {
            TestingCoffeDinoMod.LOGGER.debug("No valid target found for damage redirection - damage absorbed");

            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    player.getX(), player.getY() + 1, player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.05);
        }
    }

    private static void spawnDamageTransferParticles(Vec3 start, Vec3 end, ServerLevel level) {
        int particles = 20;
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
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
        private int ticksActive = 0;

        public LoverAbilityInstance(Player player) {
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

        public Player getPlayer() {
            return player;
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
}