package net.CoffeDino.testmod.abilities;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID)
public class EtherealCollisionHandler {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (EtherealAbilityHandler.isAbilityActive(player)) {
                player.noPhysics = true;
                player.setNoGravity(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (EtherealAbilityHandler.isAbilityActive(player)) {
                event.setCanceled(true);
            }
        }
    }
}