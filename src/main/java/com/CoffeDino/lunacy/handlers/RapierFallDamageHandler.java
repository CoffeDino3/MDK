package com.CoffeDino.lunacy.handlers;


import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.item.Custom.RapierItem;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

@EventBusSubscriber(modid = Lunacy.MODID)
public class RapierFallDamageHandler {

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && RapierItem.isWithinLungeFallWindow(player)) {
            event.setCanceled(true);
        }
    }
}