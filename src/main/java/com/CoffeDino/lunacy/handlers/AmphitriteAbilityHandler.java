package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.entity.AmphitriteOrbEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "lunacy")
public class AmphitriteAbilityHandler {

    private static final double CHECK_RADIUS = 8.0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.getData(ModAttachments.AMPHITRITE_SUMMONED.get())) return;
        if (player.level().getGameTime() % 20 != 0) return;
        boolean anyOrbsLeft = !player.level().getEntitiesOfClass(AmphitriteOrbEntity.class,
                player.getBoundingBox().inflate(CHECK_RADIUS),
                orb -> player.getUUID().equals(orb.getOwnerUUID())).isEmpty();

        if (!anyOrbsLeft) {
            player.setData(ModAttachments.AMPHITRITE_SUMMONED.get(), false);
        }
    }
}