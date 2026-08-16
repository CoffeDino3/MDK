package com.CoffeDino.lunacy.abilities;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.effects.AbilityCooldown;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Lunacy.MODID)
public class PhantomAbilityHandler {
    private static final Map<UUID, PhantomAbilityInstance> ACTIVE_ABILITIES = new HashMap<>();
    private static final int COOLDOWN_TICKS = 60;
    private static final double BASE_LAUNCH_VELOCITY = 2.0;
    private static final double LAUNCH_GROWTH_PER_10_LEVELS = 0.10;
    private static final int LAUNCH_DURATION = 30;
    private static final float BASE_GLIDE_SPEED = 0.3f;
    private static final float BOOSTED_GLIDE_SPEED = 0.6f;
    private static final float BASE_DESCENT_SPEED = -0.05f;
    private static final int MID_AIR_JUMP_UNLOCK_LEVEL = 25;
    private static final double MID_AIR_JUMP_VELOCITY = 1.1;
    private static final int MID_AIR_JUMP_COOLDOWN_TICKS = 40;
    private static final float AIRBORNE_ATTACK_DAMAGE_MULTIPLIER = 1.75f;

    private static final Map<UUID, Long> lastMidAirJumpTick = new HashMap<>();

    private static double getLaunchVelocity(int level) {
        return BASE_LAUNCH_VELOCITY * (1.0 + (level / 10) * LAUNCH_GROWTH_PER_10_LEVELS);
    }

    public static void activateAbility(Player player) {
        if (player.level().isClientSide()) return;
        UUID playerId = player.getUUID();

        PhantomAbilityInstance existing = ACTIVE_ABILITIES.get(playerId);
        if (existing != null) {
            if (player instanceof ServerPlayer sp) {
                tryMidAirJump(sp);
            }
            return;
        }
        if (AbilityCooldown.isActive(player, ModEffects.PHANTOM_COOLDOWN)) {
            return;
        }
        int level = (player instanceof ServerPlayer sp) ? PlayerLevels.getLevel(sp) : 1;
        ACTIVE_ABILITIES.put(playerId, new PhantomAbilityInstance((ServerPlayer) player, level));
        Lunacy.LOGGER.debug("Phantom ability activated for player: {} (level {})", player.getName().getString(), level);
    }
    public static boolean tryMidAirJump(ServerPlayer player) {
        PhantomAbilityInstance instance = ACTIVE_ABILITIES.get(player.getUUID());
        if (instance == null) return false;
        if (instance.isLaunching()) return false;
        if (player.onGround()) return false;

        int level = PlayerLevels.getLevel(player);
        if (level < MID_AIR_JUMP_UNLOCK_LEVEL) return false;

        long currentTick = player.level().getGameTime();
        Long lastJump = lastMidAirJumpTick.get(player.getUUID());
        if (lastJump != null && currentTick - lastJump < MID_AIR_JUMP_COOLDOWN_TICKS) return false;

        instance.startMidAirJumpBoost(MID_AIR_JUMP_VELOCITY);
        player.hurtMarked = true;
        lastMidAirJumpTick.put(player.getUUID(), currentTick);
        return true;
    }
    public static boolean isAirborneForAttackBonus(Player player) {
        return isGliding(player) && !player.onGround();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ACTIVE_ABILITIES.values().removeIf(ability -> {
            boolean ended = ability.tick();
            if (ended) {
                AbilityCooldown.start(ability.getPlayer(), ModEffects.PHANTOM_COOLDOWN, COOLDOWN_TICKS);
            }
            return ended;
        });
    }
    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;
        if (races.getPlayerRace(attacker) != races.Race.PHANTOM) return;
        if (!isAirborneForAttackBonus(attacker)) return;

        int level = PlayerLevels.getLevel(attacker);
        if (level < MID_AIR_JUMP_UNLOCK_LEVEL) return;

        event.setAmount(event.getAmount() * AIRBORNE_ATTACK_DAMAGE_MULTIPLIER);
    }

    public static boolean isGliding(Player player) {
        return ACTIVE_ABILITIES.containsKey(player.getUUID());
    }

    public static void deactivateAbility(Player player) {
        ACTIVE_ABILITIES.remove(player.getUUID());
    }

    private static class PhantomAbilityInstance {
        private final ServerPlayer player;
        private final double launchVelocity;
        private int ticksActive = 0;
        private boolean isLaunching = true;
        private int midAirJumpBoostTicks = 0;
        private double midAirJumpVelocity = 0;

        public PhantomAbilityInstance(ServerPlayer player, int level) {
            this.player = player;
            this.launchVelocity = getLaunchVelocity(level);
            Vec3 currentMotion = player.getDeltaMovement();
            player.setDeltaMovement(currentMotion.x, launchVelocity, currentMotion.z);
            player.hurtMarked = true;
        }

        public ServerPlayer getPlayer() {
            return player;
        }

        public boolean isLaunching() {
            return isLaunching;
        }
        public void startMidAirJumpBoost(double velocity) {
            this.midAirJumpBoostTicks = 15;
            this.midAirJumpVelocity = velocity;
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
                    Lunacy.LOGGER.debug("Phantom glide ended: landed on ground");
                    return true;
                }
                if (player.isInWater()) {
                    Lunacy.LOGGER.debug("Phantom glide ended: entered water");
                    return true;
                }
                if (player.isShiftKeyDown()) {
                    Lunacy.LOGGER.debug("Phantom glide ended: player sneaking");
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

            if (midAirJumpBoostTicks > 0) {
                verticalSpeed = midAirJumpVelocity;
                midAirJumpVelocity *= 0.92;
                midAirJumpBoostTicks--;
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