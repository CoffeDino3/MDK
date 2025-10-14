package net.CoffeDino.testmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.network.NetworkHandler;
import net.CoffeDino.testmod.races.races;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID, value = Dist.CLIENT)
public class KeyBindHandler {
    public static final KeyMapping SCULK_STORAGE_KEY = new KeyMapping(
            "key.testingcoffedinomod.sculk_storage",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.testingcoffedinomod.abilities"
    );

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(SCULK_STORAGE_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && SCULK_STORAGE_KEY.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
                races.Race race = races.getPlayerRace(minecraft.player);
                if (race == races.Race.SCULK) {
                    NetworkHandler.openSculkStorage();
                }
            }
        }
    }
}