package com.CoffeDino.lunacy.abilities;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.effects.AbilityCooldown;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.entity.abilities.AngelbornAbilityEntity;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AngelbornAbilityHandler {
    private static final int COOLDOWN_TICKS = 240;
    private static final int MAX_PORTALS_PER_ACTIVATION = 25;
    private static final int PORTALS_PER_10_LEVELS = 1;
    private static final float DAMAGE_RATIO_GROWTH_PER_LEVEL = 0.01f;
    public static final int MINOR_PORTAL_UNLOCK_LEVEL = 75;
    public static final float EMPOWERED_DAMAGE_MULTIPLIER = 1.25f;

    public static float getDamageRatioMultiplier(Player player) {
        int level = (player instanceof ServerPlayer sp) ? PlayerLevels.getLevel(sp) : 1;
        return 1.0f + level * DAMAGE_RATIO_GROWTH_PER_LEVEL;
    }

    public static boolean isEmpowered(Player player) {
        int level = (player instanceof ServerPlayer sp) ? PlayerLevels.getLevel(sp) : 1;
        return level >= MINOR_PORTAL_UNLOCK_LEVEL;
    }

    private static int getPortalCountPerActivation(int level) {
        int extra = (level / 10) * PORTALS_PER_10_LEVELS;
        return Math.min(MAX_PORTALS_PER_ACTIVATION, 1 + extra);
    }

    public static void activateAbility(Player player) {
        if (player.level().isClientSide()) return;
        if (AbilityCooldown.isActive(player, ModEffects.ANGELBORN_COOLDOWN)) return;

        ServerLevel level = (ServerLevel) player.level();
        int playerLevel = (player instanceof ServerPlayer sp) ? PlayerLevels.getLevel(sp) : 1;
        int portalCount = getPortalCountPerActivation(playerLevel);

        List<Vec3> placedPositions = new ArrayList<>();
        for (int i = 0; i < portalCount; i++) {
            Vec3 spawnPos = getRandomSpawnPosition(player, placedPositions);
            placedPositions.add(spawnPos);

            AngelbornAbilityEntity abilityEntity = new AngelbornAbilityEntity(level, player);
            abilityEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            level.addFreshEntity(abilityEntity);
        }

        AbilityCooldown.start(player, ModEffects.ANGELBORN_COOLDOWN, COOLDOWN_TICKS);
        Lunacy.LOGGER.debug("Angelborn ability activated for player: {} - spawned {} portal(s) (level {})",
                player.getName().getString(), portalCount, playerLevel);
    }
    private static final double MIN_PORTAL_SEPARATION = 1.75;
    private static Vec3 getRandomSpawnPosition(Player player, List<Vec3> alreadyPlaced) {
        Vec3 playerPos = player.position();
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0, look.z);
        forward = forward.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0, forward.x);

        for (int attempt = 0; attempt < 25; attempt++) {
            double distance = 2.0 + Math.random() * 3.5;
            double forwardComp;
            double sideComp;
            double roll = Math.random();

            if (roll < 0.45) {
                double a = Math.toRadians(-40 + Math.random() * 80);
                forwardComp = -Math.cos(a);
                sideComp = Math.sin(a);
            } else if (roll < 0.90) {
                double side = Math.random() < 0.5 ? -1 : 1;
                double a = Math.toRadians(-30 + Math.random() * 60);
                sideComp = side * Math.cos(a);
                forwardComp = Math.sin(a) * 0.3;
            } else {
                double a = Math.toRadians(-40 + Math.random() * 80);
                forwardComp = Math.cos(a);
                sideComp = Math.sin(a);
            }

            double x = playerPos.x + (forward.x * forwardComp + right.x * sideComp) * distance;
            double z = playerPos.z + (forward.z * forwardComp + right.z * sideComp) * distance;
            double y = playerPos.y + 0.5 + Math.random() * (player.getEyeHeight() + 1.0);
            Vec3 candidate = new Vec3(x, y, z);

            boolean tooClose = false;
            for (Vec3 placed : alreadyPlaced) {
                if (placed.distanceTo(candidate) < MIN_PORTAL_SEPARATION) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) {
                return candidate;
            }
        }
        return playerPos.subtract(forward.scale(4.5)).add(0, 1.0, 0);
    }
}