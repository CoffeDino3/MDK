package net.CoffeDino.testmod.abilities;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID)
public class PhantomAbilityHandler {
    private static final Map<UUID, PhantomAbilityInstance> ACTIVE_ABILITIES = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_DURATION = 3000;
    private static final double LAUNCH_VELOCITY = 2.0;
    private static final int LAUNCH_DURATION = 30;
    private static final float BASE_GLIDE_SPEED = 0.3f;
    private static final float BOOSTED_GLIDE_SPEED = 0.6f;
    private static final float BASE_DESCENT_SPEED = -0.05f;

    public static void activateAbility(Player player) {
        if (player.level().isClientSide()) return;
        UUID playerId = player.getUUID();
        if (!canActivateAbility(player) || ACTIVE_ABILITIES.containsKey(playerId)) {
            return;
        }
        ACTIVE_ABILITIES.put(playerId, new PhantomAbilityInstance((ServerPlayer) player));
        startCooldown(player);
        TestingCoffeDinoMod.LOGGER.debug("Phantom ability activated for player: {}", player.getName().getString());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ACTIVE_ABILITIES.values().removeIf(ability -> ability.tick());
    }

    public static boolean isGliding(Player player) {
        return ACTIVE_ABILITIES.containsKey(player.getUUID());
    }

    public static void deactivateAbility(Player player) {
        ACTIVE_ABILITIES.remove(player.getUUID());
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

    private static class PhantomAbilityInstance {
        private final ServerPlayer player;
        private int ticksActive = 0;
        private boolean isLaunching = true;

        public PhantomAbilityInstance(ServerPlayer player) {
            this.player = player;
            Vec3 currentMotion = player.getDeltaMovement();
            player.setDeltaMovement(currentMotion.x, LAUNCH_VELOCITY, currentMotion.z);
            player.hurtMarked = true;

            TestingCoffeDinoMod.LOGGER.debug("Phantom launch: Velocity={}", LAUNCH_VELOCITY);
        }

        public boolean tick() {
            if (!player.isAlive() || player.level().isClientSide()) {
                return true;
            }

            ticksActive++;

            if (isLaunching) {
                handleLaunchPhase();
            } else {
                handleGliding();
                if (player.onGround()) {
                    TestingCoffeDinoMod.LOGGER.debug("Phantom glide ended: landed on ground");
                    return true;
                }
                if (player.isInWater()) {
                    TestingCoffeDinoMod.LOGGER.debug("Phantom glide ended: entered water");
                    return true;
                }
                if (player.isShiftKeyDown()) {
                    TestingCoffeDinoMod.LOGGER.debug("Phantom glide ended: player sneaking");
                    return true;
                }
            }

            return false;
        }

        private void handleLaunchPhase() {
            double currentMotionY = player.getDeltaMovement().y;
            if (ticksActive < LAUNCH_DURATION && currentMotionY > 0) {
                Vec3 motion = player.getDeltaMovement();
                double newMotionY = currentMotionY * 0.92;
                player.setDeltaMovement(motion.x, newMotionY, motion.z);
                player.hurtMarked = true;
            } else {
                if (currentMotionY <= 0 || ticksActive >= LAUNCH_DURATION) {
                    isLaunching = false;
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, BASE_DESCENT_SPEED, motion.z);
                    player.hurtMarked = true;
                    TestingCoffeDinoMod.LOGGER.debug("Transitioned to gliding at tick {}, motionY={}", ticksActive, currentMotionY);
                }
            }
        }

        private void handleGliding() {
            Vec3 lookVec = player.getLookAngle();
            Vec3 currentMotion = player.getDeltaMovement();
            float glideSpeed = player.isCrouching() ? BOOSTED_GLIDE_SPEED : BASE_GLIDE_SPEED;
            Vec3 horizontalLook = new Vec3(lookVec.x, 0, lookVec.z).normalize();
            float pitch = player.getXRot();
            double verticalSpeed = BASE_DESCENT_SPEED;

            if (pitch > 10) {
                verticalSpeed = BASE_DESCENT_SPEED * (1.0 + (pitch - 10) / 20.0);
                verticalSpeed = Math.min(verticalSpeed, -0.15);
            } else if (pitch < -10) {
                verticalSpeed = BASE_DESCENT_SPEED * (1.0 - (-pitch - 10) / 30.0);
                if (pitch < -30) {
                    verticalSpeed = Math.max(verticalSpeed, 0.05);
                } else {
                    verticalSpeed = Math.max(verticalSpeed, -0.02);
                }
            }
            double motionX = horizontalLook.x * glideSpeed;
            double motionZ = horizontalLook.z * glideSpeed;

            motionX = motionX * 0.7 + currentMotion.x * 0.3;
            motionZ = motionZ * 0.7 + currentMotion.z * 0.3;

            player.setDeltaMovement(motionX, verticalSpeed, motionZ);
            player.hurtMarked = true;
            player.fallDistance = 0.0f;
            if (ticksActive % 3 == 0) {
                spawnGlideParticles();
            }

            TestingCoffeDinoMod.LOGGER.debug("Gliding: Motion=({}, {}, {}), Pitch={}",
                    String.format("%.3f", motionX), String.format("%.3f", verticalSpeed),
                    String.format("%.3f", motionZ), pitch);
        }

        private void spawnGlideParticles() {
            Vec3 pos = player.position();
            ServerLevel level = (ServerLevel) player.level();
            Vec3 lookVec = player.getLookAngle().normalize();
            Vec3 particlePos = pos.subtract(lookVec.x * 0.8, 0.5, lookVec.z * 0.8);

            level.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
                    particlePos.x, particlePos.y, particlePos.z,
                    3, 0.2, 0.2, 0.2, 0.02);
            if (player.isCrouching()) {
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        particlePos.x, particlePos.y, particlePos.z,
                        2, 0.15, 0.15, 0.15, 0.03);
            }
        }
    }
}