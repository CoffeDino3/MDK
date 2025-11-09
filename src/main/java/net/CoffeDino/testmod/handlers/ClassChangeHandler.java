package net.CoffeDino.testmod.handlers;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ClassChangeHandler {

    @SubscribeEvent
    public static void onClassChange(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide()) {
            // Check for guns when player joins world
            InventoryHandler.checkAndRemoveGun(player);
        }
    }
}