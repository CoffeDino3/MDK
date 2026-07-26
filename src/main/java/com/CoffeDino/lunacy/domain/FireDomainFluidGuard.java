package com.CoffeDino.lunacy.domain;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = "lunacy")
public class FireDomainFluidGuard {

    @SubscribeEvent
    public static void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        for (FireDomain d : FireDomainManager.allDomains()) {
            if (d.level != event.getLevel()) continue;
            double dx = event.getPos().getX() - d.center.getX();
            double dz = event.getPos().getZ() - d.center.getZ();
            double guard = d.radius + 3;
            if (dx * dx + dz * dz <= guard * guard) {
                event.setCanceled(true);
                return;
            }
        }
    }
}