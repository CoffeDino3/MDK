package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.domain.FireDomainManager;
import com.CoffeDino.lunacy.item.Custom.FireSpearItem;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = "lunacy")
public class FireDomainInterruptHandler {

    @SubscribeEvent
    public static void onDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof FireSpearItem)) return;
        if (!FireDomainManager.hasActiveDomain(player.getUUID())) return;

        FireDomainManager.interruptCharge(player);
    }
}