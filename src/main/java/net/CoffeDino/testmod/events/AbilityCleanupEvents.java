// Add this to your existing event handler class, or create a new one
package net.CoffeDino.testmod.events;

import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.abilities.BelieverAbilityHandler;
import net.CoffeDino.testmod.races.races;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Lunacy.MOD_ID)
public class AbilityCleanupEvents {

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        BelieverAbilityHandler.onPlayerLogout(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        BelieverAbilityHandler.onPlayerLogout(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        BelieverAbilityHandler.onPlayerLogout(event.getEntity());
    }
}