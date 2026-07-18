package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Lunacy.MODID)
public class VampireSunlightHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        races.handleVampireSunlight(player);
    }
}