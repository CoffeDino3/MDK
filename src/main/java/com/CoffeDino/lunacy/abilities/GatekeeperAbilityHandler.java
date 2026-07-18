package com.CoffeDino.lunacy.abilities;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.entity.abilities.GatekeeperPortalEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = Lunacy.MODID)
public class GatekeeperAbilityHandler {
    private static final int MAX_PORTALS = 30;
    private static final int PORTAL_SPAWN_INTERVAL = 50;
    private static final double DISTANCE_BEHIND = 2.5;
    private static final double GRID_SPACING = 1.2;
    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 10;
    private static final double HUNGER_PER_PORTAL_PER_SECOND = 0.1;
    private static final ResourceLocation MOVEMENT_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "gatekeeper_immobilize");

    private static final Map<UUID, GatekeeperAbilityData> ACTIVE_PLAYERS = new HashMap<>();

    private record GatekeeperAbilityData(
            ServerPlayer player,
            List<GatekeeperPortalEntity> portals,
            AttributeModifier movementModifier,
            int nextPortalIndex,
            int hungerTimer
    ) {
        public GatekeeperAbilityData(ServerPlayer player, AttributeModifier movementModifier) {
            this(player, new ArrayList<>(), movementModifier, 0, 0);
        }
    }

    public static void activateAbility(ServerPlayer player) {
        try {
            if (ACTIVE_PLAYERS.containsKey(player.getUUID())) return;
            if (player.getFoodData().getFoodLevel() <= 0) return;

            AttributeModifier movementModifier = new AttributeModifier(
                    MOVEMENT_MODIFIER_ID,
                    -1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

            ACTIVE_PLAYERS.put(player.getUUID(), new GatekeeperAbilityData(player, movementModifier));
            Lunacy.LOGGER.debug("Gatekeeper ability activated for {}", player.getName().getString());
        } catch (Exception e) {
            Lunacy.LOGGER.error("Failed to activate Gatekeeper ability", e);
        }
    }

    public static void deactivateAbility(Player player) {
        GatekeeperAbilityData data = ACTIVE_PLAYERS.remove(player.getUUID());
        if (data != null) {
            cleanupAbility(player, data.portals(), data.movementModifier());
        }
    }

    private static void cleanupAbility(Player player, List<GatekeeperPortalEntity> portals, AttributeModifier modifier) {
        for (GatekeeperPortalEntity portal : portals) {
            portal.discard();
        }
        if (player instanceof ServerPlayer serverPlayer) {
            AttributeInstance movement = serverPlayer.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movement != null) {
                movement.removeModifier(modifier);
            }
        }
        Lunacy.LOGGER.debug("Gatekeeper ability cleaned up for {}", player.getName().getString());
    }

    public static boolean isActive(Player player) {
        return ACTIVE_PLAYERS.containsKey(player.getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<UUID, GatekeeperAbilityData>> iterator = ACTIVE_PLAYERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, GatekeeperAbilityData> entry = iterator.next();
            GatekeeperAbilityData data = entry.getValue();
            ServerPlayer player = data.player();
            if (player.isRemoved() || !player.isAlive()) {
                cleanupAbility(player, data.portals(), data.movementModifier());
                iterator.remove();
                continue;
            }
            int hungerTimer = data.hungerTimer() + 1;
            int nextIndex = data.nextPortalIndex();
            List<GatekeeperPortalEntity> portals = data.portals();
            portals.removeIf(GatekeeperPortalEntity::isRemoved);
            if (hungerTimer >= PORTAL_SPAWN_INTERVAL) {
                hungerTimer = 0;
                if (portals.size() < MAX_PORTALS) {
                    Vec3 pos = calculatePortalPosition(player, nextIndex, portals);
                    GatekeeperPortalEntity portal = new GatekeeperPortalEntity(player.serverLevel(), player, pos);
                    player.serverLevel().addFreshEntity(portal);
                    portals.add(portal);
                    nextIndex++;
                }
            }
            FoodData food = player.getFoodData();
            float exhaustion = (float) (portals.size() * HUNGER_PER_PORTAL_PER_SECOND * 4.0 /20);
            food.addExhaustion(exhaustion);
            if (food.getFoodLevel() <= 0) {
                cleanupAbility(player, portals, data.movementModifier());
                iterator.remove();
                continue;
            }
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookTarget = eyePos.add(player.getLookAngle().scale(50.0));
            for (GatekeeperPortalEntity portal : portals) {
                Vec3 toTarget = lookTarget.subtract(portal.position()).normalize();
                float yaw = (float) Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));
                float pitch = (float) Math.toDegrees(Math.asin(Math.clamp(-toTarget.y, -1.0, 1.0)));
                portal.setFacingYaw(yaw);
                portal.setFacingPitch(pitch);
            }
            GatekeeperAbilityData newData = new GatekeeperAbilityData(player, portals, data.movementModifier(), nextIndex, hungerTimer);
            entry.setValue(newData);
        }
    }

    private static Vec3 calculatePortalPosition(ServerPlayer player, int index, List<GatekeeperPortalEntity> existing) {
        Vec3 look = player.getLookAngle();
        Vec3 back = new Vec3(-look.x, 0, -look.z).normalize();
        if (back.lengthSqr() < 0.01) back = new Vec3(0, 0, -1);
        Vec3 right = new Vec3(back.z, 0, -back.x).normalize();
        Vec3 torsoPos = player.position().add(0, 1.0, 0);
        for (int attempt = 0; attempt < 20; attempt++) {
            Random r = new Random(index * 0x9e3779b97f4a7c15L + attempt * 31L + player.getUUID().getLeastSignificantBits());
            double sideOffset  = (r.nextDouble() - 0.5) * 16.0;
            double heightOffset = r.nextDouble() * 10.0;
            double backOffset  = 1.0 + r.nextDouble() * 2.0;

            Vec3 candidate = torsoPos
                    .add(back.scale(backOffset))
                    .add(right.scale(sideOffset))
                    .add(0, heightOffset, 0);
            boolean tooClose = existing.stream().anyMatch(p -> {
                Vec3 diff = p.position().subtract(candidate);
                double lateralDist = Math.sqrt(diff.x * diff.x + diff.y * diff.y + diff.z * diff.z);
                return lateralDist < 4.0;
            });

            if (!tooClose) return candidate;
        }
        Random rng = new Random(index * 0x9e3779b97f4a7c15L + player.getUUID().getLeastSignificantBits());
        return torsoPos
                .add(back.scale(1.5))
                .add(right.scale((rng.nextDouble() - 0.5) * 16.0))
                .add(0, rng.nextDouble() * 10.0, 0);
    }
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            deactivateAbility(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            deactivateAbility(player);
        }
    }
}