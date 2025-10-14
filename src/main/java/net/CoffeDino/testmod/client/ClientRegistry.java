package net.CoffeDino.testmod.client;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.client.gui.SculkStorageScreen;
import net.CoffeDino.testmod.menu.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistry {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.SCULK_STORAGE.get(), SculkStorageScreen::new);
        });
    }
}