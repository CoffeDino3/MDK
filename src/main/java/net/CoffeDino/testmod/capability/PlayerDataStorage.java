package net.CoffeDino.testmod.capability;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.CoffeDino.testmod.Lunacy.LOGGER;

@Mod.EventBusSubscriber(modid = "lunacy")
public class PlayerDataStorage {

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        Player player = event.getEntity();
        player.getCapability(Lunacy.SCULK_STORAGE).ifPresent(storage -> {
            LOGGER.debug("Saving sculk storage data for player");
        });
    }

    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        Player player = event.getEntity();
        player.getCapability(Lunacy.SCULK_STORAGE).ifPresent(storage -> {
            LOGGER.debug("Loading sculk storage data for player");
        });
    }
}