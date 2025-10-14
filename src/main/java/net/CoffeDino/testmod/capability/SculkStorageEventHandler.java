package net.CoffeDino.testmod.capability;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID)
public class SculkStorageEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SculkStorageEventHandler.class);

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "sculk_storage"),
                    new SculkStorageProvider()
            );
            LOGGER.debug("Attached sculk storage capability to player");
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        original.reviveCaps();

        original.getCapability(ModCapabilities.SCULK_STORAGE).ifPresent(oldStorage -> {
            newPlayer.getCapability(ModCapabilities.SCULK_STORAGE).ifPresent(newStorage -> {
                newStorage.setRows(oldStorage.getRows());
                for (int i = 0; i < oldStorage.getContainerSize(); i++) {
                    ItemStack stack = oldStorage.getItem(i);
                    if (!stack.isEmpty()) {
                        newStorage.setItem(i, stack.copy());
                    }
                }

                LOGGER.info("DEBUG: Direct copied {} items from old storage to new storage",
                        oldStorage.getItemCount());
            });
        });

        original.invalidateCaps();
    }

    private static int countItems(ISculkStorage storage) {
        int count = 0;
        for (int i = 0; i < storage.getContainerSize(); i++) {
            if (!storage.getItem(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        Player player = (Player) event.getEntity();
        player.getCapability(TestingCoffeDinoMod.SCULK_STORAGE).ifPresent(storage -> {
            LOGGER.info("Player logged in. Storage has {} rows and {} items",
                    storage.getRows(), countItems(storage));
        });
    }

    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        Player player = (Player) event.getEntity();
        player.getCapability(TestingCoffeDinoMod.SCULK_STORAGE).ifPresent(storage -> {
            LOGGER.info("Player respawned. Storage has {} rows and {} items",
                    storage.getRows(), countItems(storage));
        });
    }
}