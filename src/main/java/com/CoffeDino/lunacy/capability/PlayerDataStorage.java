package com.CoffeDino.lunacy.capability;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import static com.CoffeDino.lunacy.Lunacy.LOGGER;

@EventBusSubscriber(modid = Lunacy.MODID)
public class PlayerDataStorage {

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        Player player = event.getEntity();
        SculkStorage storage = player.getData(ModAttachments.SCULK_STORAGE);
        LOGGER.debug("Saving sculk storage data for player");
    }

    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        Player player = event.getEntity();
        SculkStorage storage = player.getData(ModAttachments.SCULK_STORAGE);
        LOGGER.debug("Loading sculk storage data for player");
    }
}