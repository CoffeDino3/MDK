package com.CoffeDino.lunacy.capability;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Lunacy.MODID)
public class PlayerRespawnHandler {

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        player.getData(ModAttachments.SCULK_STORAGE).startCooldown();

        if (player.containerMenu != null && !(player.containerMenu instanceof net.minecraft.world.inventory.InventoryMenu)) {
            player.closeContainer();
        }

        Lunacy.LOGGER.debug("DEBUG: Player respawn handled for " + player.getName().getString());
    }
}