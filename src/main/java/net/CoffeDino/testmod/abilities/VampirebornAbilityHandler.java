package net.CoffeDino.testmod.abilities;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID)
public class VampirebornAbilityHandler {
    private static final Map<UUID, VampirebornAbilityInstance> ACTIVE_ABILITIES = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_DURATION = 1000;

    private static final DustParticleOptions RED_DUST = new DustParticleOptions(
            new Vector3f(0.8f, 0.0f, 0.0f), 1.0f);

    private static final float BASE_DAMAGE = 5.0f;
    private static final float DAMAGE_PER_EXTRA_PARTICLE = 1.0f;
    private static final float SELF_DAMAGE_PER_PARTICLE = 0.5f;
    private static final int MAX_RANGE = 50;
    private static final float AUTO_FIRE_HEALTH_THRESHOLD = 2.0f;

    public static void startHoldingAbility(Player player) {
        if (player.level().isClientSide()) return;
        UUID playerId = player.getUUID();
        if (ACTIVE_ABILITIES.containsKey(playerId)) {
            ACTIVE_ABILITIES.remove(playerId);
        }

        VampirebornAbilityInstance ability = new VampirebornAbilityInstance((ServerPlayer) player);
        ACTIVE_ABILITIES.put(playerId, ability);

        TestingCoffeDinoMod.LOGGER.debug("Vampireborn hold started for player: {}", player.getName().getString());
    }

    public static void fireSingleShot(Player player) {
        if (player.level().isClientSide()) return;
        if (!canActivateAbility(player)) return;
        UUID playerId = player.getUUID();
        if (ACTIVE_ABILITIES.containsKey(playerId)) {
            ACTIVE_ABILITIES.remove(playerId);
        }

        VampirebornAbilityInstance ability = new VampirebornAbilityInstance((ServerPlayer) player);
        ability.addParticle();
        ability.fireProjectile();
        startCooldown(player);

        TestingCoffeDinoMod.LOGGER.debug("Vampireborn single shot fired");
    }

    public static void releaseHeldAbility(Player player) {
        UUID playerId = player.getUUID();
        VampirebornAbilityInstance ability = ACTIVE_ABILITIES.get(playerId);

        if (ability != null && ability.getParticleCount() > 0) {
            ability.fireProjectile();
            ACTIVE_ABILITIES.remove(playerId);
            startCooldown(player);

            TestingCoffeDinoMod.LOGGER.debug("Vampireborn held ability released with {} particles",
                    ability.getParticleCount());
        } else {
            ACTIVE_ABILITIES.remove(playerId);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (VampirebornAbilityInstance ability : ACTIVE_ABILITIES.values()) {
            ability.update();
        }
        ACTIVE_ABILITIES.entrySet().removeIf(entry -> {
            VampirebornAbilityInstance ability = entry.getValue();
            if (ability.shouldAutoFire()) {
                ability.fireProjectile();
                startCooldown(ability.getPlayer());
                TestingCoffeDinoMod.LOGGER.debug("Vampireborn auto-fired with {} particles",
                        ability.getParticleCount());
                return true;
            }
            return false;
        });
    }

    public static boolean isAbilityActive(Player player) {
        return ACTIVE_ABILITIES.containsKey(player.getUUID());
    }

    public static boolean canActivateAbility(Player player) {
        UUID playerId = player.getUUID();
        Long lastUsed = COOLDOWNS.get(playerId);
        return lastUsed == null || System.currentTimeMillis() - lastUsed >= COOLDOWN_DURATION;
    }

    public static void startCooldown(Player player) {
        COOLDOWNS.put(player.getUUID(), System.currentTimeMillis());
    }

    private static class VampirebornAbilityInstance {
        private final ServerPlayer player;
        private int particleCount = 0;
        private long lastParticleAddTime = 0;
        private static final long PARTICLE_ADD_INTERVAL = 500;

        public VampirebornAbilityInstance(ServerPlayer player) {
            this.player = player;
            this.lastParticleAddTime = System.currentTimeMillis();
        }

        public void update() {
            if (!player.isAlive()) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastParticleAddTime >= PARTICLE_ADD_INTERVAL) {
                addParticle();
            }
            updateSpinningParticles();
        }

        public void addParticle() {
            particleCount++;
            lastParticleAddTime = System.currentTimeMillis();

            // Damage player
            player.hurt(player.damageSources().magic(), SELF_DAMAGE_PER_PARTICLE);

            TestingCoffeDinoMod.LOGGER.debug("Vampireborn particle added. Count: {}", particleCount);
        }

        public void fireProjectile() {
            if (particleCount == 0) return;

            float totalDamage = BASE_DAMAGE + (Math.max(0, particleCount - 1) * DAMAGE_PER_EXTRA_PARTICLE);
            createProjectile(totalDamage);

            TestingCoffeDinoMod.LOGGER.debug("Vampireborn fired with {} particles, damage: {}",
                    particleCount, totalDamage);
        }

        private void createProjectile(float damage) {
            ServerLevel level = (ServerLevel) player.level();
            Vec3 startPos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            Vec3 endPos = startPos.add(lookVec.scale(MAX_RANGE));

            ClipContext context = new ClipContext(startPos, endPos,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
            BlockHitResult blockHit = level.clip(context);

            EntityHitResult entityHit = findEntityHit(level, startPos, lookVec);

            Vec3 hitPos = endPos;
            if (blockHit.getType() != HitResult.Type.MISS) {
                hitPos = blockHit.getLocation();
            }
            if (entityHit != null) {
                double entityDist = startPos.distanceTo(entityHit.getLocation());
                double blockDist = blockHit.getType() != HitResult.Type.MISS ?
                        startPos.distanceTo(blockHit.getLocation()) : Double.MAX_VALUE;

                if (entityDist < blockDist) {
                    hitPos = entityHit.getLocation();
                }
            }
            spawnParticleTrail(level, startPos, hitPos);
            if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
                target.hurt(player.damageSources().indirectMagic(player, player), damage);
                for (int i = 0; i < 8; i++) {
                    Vec3 offset = new Vec3(
                            (Math.random() - 0.5) * 0.8,
                            (Math.random() - 0.5) * 0.8,
                            (Math.random() - 0.5) * 0.8
                    );
                    level.sendParticles(RED_DUST,
                            hitPos.x + offset.x, hitPos.y + offset.y, hitPos.z + offset.z,
                            1, 0, 0, 0, 0);
                }
            }
        }

        private EntityHitResult findEntityHit(ServerLevel level, Vec3 start, Vec3 direction) {
            AABB searchBox = new AABB(start, start.add(direction.scale(MAX_RANGE))).inflate(1.0);

            Entity closest = null;
            double closestDist = Double.MAX_VALUE;

            for (Entity entity : level.getEntities(player, searchBox)) {
                if (entity instanceof LivingEntity && entity != player && entity.isAlive()) {
                    AABB entityBox = entity.getBoundingBox().inflate(0.3);
                    Vec3 hitPos = entityBox.clip(start, start.add(direction.scale(MAX_RANGE))).orElse(null);
                    if (hitPos != null) {
                        double dist = start.distanceTo(hitPos);
                        if (dist < closestDist) {
                            closestDist = dist;
                            closest = entity;
                        }
                    }
                }
            }

            return closest != null ? new EntityHitResult(closest) : null;
        }

        private void spawnParticleTrail(ServerLevel level, Vec3 start, Vec3 end) {
            double distance = start.distanceTo(end);
            int particles = Math.max(5, (int) (distance * 3));
            Vec3 step = end.subtract(start).scale(1.0 / particles);
            for (int i = 0; i <= particles; i++) {
                Vec3 pos = start.add(step.scale(i));
                level.sendParticles(RED_DUST, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0.02);
            }
        }

        private void updateSpinningParticles() {
            if (particleCount == 0) return;
            ServerLevel level = (ServerLevel) player.level();
            Vec3 center = player.getEyePosition().add(player.getLookAngle().scale(1.5));
            Vec3 up = new Vec3(0, 1, 0);
            Vec3 lookVec = player.getLookAngle();
            Vec3 right = lookVec.cross(up).normalize();
            if (right.length() < 0.1) {
                right = new Vec3(1, 0, 0);
            }
            up = right.cross(lookVec).normalize();

            double time = System.currentTimeMillis() * 0.003;
            double radius = 0.5 + (particleCount * 0.003);

            for (int i = 0; i < particleCount; i++) {
                double angle = time + (2 * Math.PI * i) / particleCount;
                Vec3 offset = right.scale(Math.cos(angle) * radius)
                        .add(up.scale(Math.sin(angle) * radius));
                Vec3 pos = center.add(offset);

                level.sendParticles(RED_DUST, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            }
        }

        public boolean shouldAutoFire() {
            return player.getHealth() <= AUTO_FIRE_HEALTH_THRESHOLD && particleCount > 0;
        }

        public Player getPlayer() {
            return player;
        }

        public int getParticleCount() {
            return particleCount;
        }
    }
}