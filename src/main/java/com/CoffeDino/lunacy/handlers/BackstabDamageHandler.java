package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.item.Custom.DaggerItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = "lunacy")
public class BackstabDamageHandler {

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();

        if (!(event.getSource().getEntity() instanceof Player attacker)) return;
        if (!(attacker.getMainHandItem().getItem() instanceof DaggerItem dagger)) return;

        if (dagger.isBackstab(target, attacker)) {
            event.setAmount(event.getAmount() * dagger.getBackstabMultiplier());
        }
    }
}
