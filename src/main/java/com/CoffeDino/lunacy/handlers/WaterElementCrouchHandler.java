package com.CoffeDino.lunacy.handlers;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class WaterElementCrouchHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        var player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (player.isShiftKeyDown() && player.isOnFire()) {
            player.setRemainingFireTicks(0);
        }
    }
}