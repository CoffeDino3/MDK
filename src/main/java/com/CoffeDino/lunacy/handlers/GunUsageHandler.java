package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.entity.LamentBulletEntity;
import com.CoffeDino.lunacy.item.Custom.GunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Lunacy.MODID)
public class GunUsageHandler {
    private static final Map<UUID, Integer> playerAimTicks = new HashMap<>();
    private static final int MAX_AIM_TICKS = 20;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.getItem() instanceof GunItem && player.isUsingItem()) {
            UUID playerId = player.getUUID();
            int currentTicks = playerAimTicks.getOrDefault(playerId, 0);

            if (currentTicks < MAX_AIM_TICKS) {
                playerAimTicks.put(playerId, currentTicks + 1);
            }
        } else {
            playerAimTicks.remove(player.getUUID());
        }
    }

    public static float getAimProgress(Player player) {
        int ticks = playerAimTicks.getOrDefault(player.getUUID(), 0);
        return Math.min((float) ticks / MAX_AIM_TICKS, 1.0f);
    }

    public static boolean isFullyAimed(Player player) {
        return getAimProgress(player) >= 1.0f;
    }
}