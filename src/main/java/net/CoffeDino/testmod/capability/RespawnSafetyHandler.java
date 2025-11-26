package net.CoffeDino.testmod.capability;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Lunacy.MOD_ID)
public class RespawnSafetyHandler {

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(Lunacy.SCULK_STORAGE).ifPresent(storage -> {
                storage.startCooldown();
            });

            player.refreshDimensions();

            System.out.println("DEBUG: Respawn safety measures applied for " + player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(Lunacy.SCULK_STORAGE).ifPresent(storage -> {
            });
        }
    }

}