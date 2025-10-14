package net.CoffeDino.testmod.capability;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID)
public class ModEventHandlers {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(RaceSizeProvider.RACE_SIZE).isPresent()) {
                event.addCapability(
                        ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "race_size"),
                        new RaceSizeProvider()
                );
                System.out.println("DEBUG: Attached race size capability to player");
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        original.reviveCaps();

        original.getCapability(RaceSizeProvider.RACE_SIZE).ifPresent(oldCap -> {
            newPlayer.getCapability(RaceSizeProvider.RACE_SIZE).ifPresent(newCap -> {
                HolderLookup.Provider lookup = newPlayer.level().registryAccess();
                CompoundTag nbt = ((RaceSizeCapability) oldCap).serializeNBT(lookup);
                ((RaceSizeCapability) newCap).deserializeNBT(lookup, nbt);
            });
        });

        original.invalidateCaps();
        System.out.println("DEBUG: Copied race size capability on player clone");
    }
}