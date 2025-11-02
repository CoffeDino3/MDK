package net.CoffeDino.testmod.effects;

import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EffectEventHandlers {

    @SubscribeEvent
    public static void onLivingDamageBloodSurge(LivingDamageEvent event) {
        BloodSurgeEffect.handleLivingDamage(event);
    }

    @SubscribeEvent
    public static void onLivingDamageEther(LivingDamageEvent event) {
        EtherEffect.handleLivingDamage(event);
    }
}