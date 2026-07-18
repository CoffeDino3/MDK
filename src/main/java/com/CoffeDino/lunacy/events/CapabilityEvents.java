package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.player.ReaperSoulData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Lunacy.MODID)
public class CapabilityEvents {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        ReaperSoulData oldData = original.getData(ModAttachments.REAPER_SOUL);
        ReaperSoulData newData = newPlayer.getData(ModAttachments.REAPER_SOUL);

        newData.setSoulStacks(oldData.getSoulStacks());
        newData.setLastGainTime(oldData.getLastGainTime());
    }
}