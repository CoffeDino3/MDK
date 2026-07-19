package com.CoffeDino.lunacy.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

@EventBusSubscriber(modid = "lunacy")
public class GruckFallDamageHandler {

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player
                && player.getPersistentData().getBoolean("GruckNoFallDamage")) {
            event.setCanceled(true);
            player.getPersistentData().remove("GruckNoFallDamage");
            player.fallDistance = 0F;
        }
    }
}