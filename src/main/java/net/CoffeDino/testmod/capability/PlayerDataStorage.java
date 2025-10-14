package net.CoffeDino.testmod.capability;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.CoffeDino.testmod.TestingCoffeDinoMod.LOGGER;

@Mod.EventBusSubscriber(modid = "testingcoffedinomod")
public class PlayerDataStorage {

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        Player player = event.getEntity();
        player.getCapability(TestingCoffeDinoMod.SCULK_STORAGE).ifPresent(storage -> {
            LOGGER.debug("Saving sculk storage data for player");
        });
    }

    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        Player player = event.getEntity();
        player.getCapability(TestingCoffeDinoMod.SCULK_STORAGE).ifPresent(storage -> {
            LOGGER.debug("Loading sculk storage data for player");
        });
    }
}