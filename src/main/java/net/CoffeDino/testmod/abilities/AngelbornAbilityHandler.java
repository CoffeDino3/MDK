package net.CoffeDino.testmod.abilities;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.entity.abilities.AngelbornAbilityEntity;
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
public class AngelbornAbilityHandler {
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_DURATION = 12000;

    public static void activateAbility(Player player) {
        if (player.level().isClientSide()) return;
        if (!canActivateAbility(player)) return;

        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel level = (ServerLevel) player.level();
        Vec3 spawnPos = getRandomSpawnPosition(player);

        AngelbornAbilityEntity abilityEntity = new AngelbornAbilityEntity(level, player);
        abilityEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        level.addFreshEntity(abilityEntity);

        startCooldown(player);
        TestingCoffeDinoMod.LOGGER.debug("Angelborn ability activated for player: {}", player.getName().getString());
    }

    private static Vec3 getRandomSpawnPosition(Player player) {
        Vec3 playerPos = player.position();
        Vec3 lookVec = player.getLookAngle();

        double angle = Math.random() * Math.PI * 1.5;
        double distance = 2.0 + Math.random() * 3.0;
        double x = playerPos.x + Math.cos(angle) * distance;
        double z = playerPos.z + Math.sin(angle) * distance;
        double y = playerPos.y + 0.5 + Math.random() * (player.getEyeHeight() - 0.5);
        Vec3 result = new Vec3(x, y, z);
        Vec3 toPlayer = playerPos.subtract(result);
        if (toPlayer.length() < 0.5) {
            result = playerPos.add(toPlayer.normalize().scale(0.5));
        }

        return result;
    }

    public static boolean canActivateAbility(Player player) {
        UUID playerId = player.getUUID();
        Long lastUsed = COOLDOWNS.get(playerId);
        return lastUsed == null || System.currentTimeMillis() - lastUsed >= COOLDOWN_DURATION;
    }

    private static void startCooldown(Player player) {
        COOLDOWNS.put(player.getUUID(), System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            long currentTime = System.currentTimeMillis();
            COOLDOWNS.entrySet().removeIf(entry ->
                    currentTime - entry.getValue() >= COOLDOWN_DURATION);
        }
    }
}