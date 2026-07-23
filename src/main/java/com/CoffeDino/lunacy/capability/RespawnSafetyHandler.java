package com.CoffeDino.lunacy.capability;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Lunacy.MODID)
public class RespawnSafetyHandler {

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getData(ModAttachments.SCULK_STORAGE).startCooldown();
            player.refreshDimensions();
            Lunacy.LOGGER.debug("DEBUG: Respawn safety measures applied for " + player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getData(ModAttachments.SCULK_STORAGE);
        }
    }
}