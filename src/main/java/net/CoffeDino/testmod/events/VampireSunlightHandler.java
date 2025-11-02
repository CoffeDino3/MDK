package net.CoffeDino.testmod.events;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.races.races;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID)
public class VampireSunlightHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            races.handleVampireSunlight(event.player);
        }
    }
}