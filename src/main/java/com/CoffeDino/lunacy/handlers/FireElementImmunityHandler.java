package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.classes.ClassDataManager;
import com.CoffeDino.lunacy.classes.SpellbladeElement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = "lunacy")
public class FireElementImmunityHandler {

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getSource().is(DamageTypes.ON_FIRE) || event.getSource().is(DamageTypes.IN_FIRE))) return;
        ClassDataManager dataManager = ClassDataManager.get(player);
        String elementId = dataManager.getPlayerElement(player.getUUID());
        SpellbladeElement element = SpellbladeElement.fromId(elementId);
        if (element == SpellbladeElement.FIRE) {
            event.setCanceled(true);
        }
    }
}