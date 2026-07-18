package com.CoffeDino.lunacy.capability;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = Lunacy.MODID)
public class SculkStorageEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SculkStorageEventHandler.class);

    /**
     * AttachCapabilitiesEvent is gone — SCULK_STORAGE is a Data Attachment registered in
     * ModAttachments and auto-initialized on first getData() call. No explicit attach needed.
     */

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        // No reviveCaps() / invalidateCaps() — NeoForge manages attachment lifetime.
        SculkStorage oldStorage = original.getData(ModAttachments.SCULK_STORAGE);
        SculkStorage newStorage = newPlayer.getData(ModAttachments.SCULK_STORAGE);

        newStorage.setRows(oldStorage.getRows());
        for (int i = 0; i < oldStorage.getContainerSize(); i++) {
            ItemStack stack = oldStorage.getItem(i);
            if (!stack.isEmpty()) {
                newStorage.setItem(i, stack.copy());
            }
        }

        LOGGER.info("DEBUG: Direct copied {} items from old storage to new storage",
                oldStorage.getItemCount());
    }

    private static int countItems(ISculkStorage storage) {
        int count = 0;
        for (int i = 0; i < storage.getContainerSize(); i++) {
            if (!storage.getItem(i).isEmpty()) count++;
        }
        return count;
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = (Player) event.getEntity();
        SculkStorage storage = player.getData(ModAttachments.SCULK_STORAGE);
        LOGGER.info("Player logged in. Storage has {} rows and {} items",
                storage.getRows(), countItems(storage));
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = (Player) event.getEntity();
        SculkStorage storage = player.getData(ModAttachments.SCULK_STORAGE);
        LOGGER.info("Player respawned. Storage has {} rows and {} items",
                storage.getRows(), countItems(storage));
    }
}