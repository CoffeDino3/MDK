package com.CoffeDino.lunacy.capability;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Lunacy.MODID)
public class ModEventHandlers {

    /**
     * AttachCapabilitiesEvent is gone in NeoForge — Data Attachments are lazily created
     * on first getData() call, so no explicit attach step is needed for RACE_SIZE.
     * The attachment is auto-initialized via RaceSizeCapability::new (see ModAttachments).
     */

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        // No reviveCaps() / invalidateCaps() needed — NeoForge handles attachment
        // lifetime automatically. Just copy the data directly.
        RaceSizeCapability oldCap = original.getData(ModAttachments.RACE_SIZE);
        RaceSizeCapability newCap = newPlayer.getData(ModAttachments.RACE_SIZE);

        HolderLookup.Provider lookup = newPlayer.level().registryAccess();
        CompoundTag nbt = oldCap.serializeNBT(lookup);
        newCap.deserializeNBT(lookup, nbt);

        System.out.println("DEBUG: Copied race size attachment on player clone");
    }
}