package net.CoffeDino.testmod.capability;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Lunacy.MOD_ID)
public class PlayerRespawnHandler {

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        player.getCapability(Lunacy.SCULK_STORAGE).ifPresent(storage -> {
            storage.startCooldown();
        });

        if (player.containerMenu != null && !(player.containerMenu instanceof net.minecraft.world.inventory.InventoryMenu)) {
            player.closeContainer();
        }

        System.out.println("DEBUG: Player respawn handled for " + player.getName().getString());
    }
}