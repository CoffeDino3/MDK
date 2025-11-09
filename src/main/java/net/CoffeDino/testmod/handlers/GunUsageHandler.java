package net.CoffeDino.testmod.handlers;

import net.CoffeDino.testmod.entity.LamentBulletEntity;
import net.CoffeDino.testmod.item.Custom.GunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class GunUsageHandler {
    private static final Map<UUID, Integer> playerAimTicks = new HashMap<>();
    private static final int MAX_AIM_TICKS = 20;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
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
    }

    public static float getAimProgress(Player player) {
        int ticks = playerAimTicks.getOrDefault(player.getUUID(), 0);
        return Math.min((float) ticks / MAX_AIM_TICKS, 1.0f);
    }

    public static boolean isFullyAimed(Player player) {
        return getAimProgress(player) >= 1.0f;
    }
}