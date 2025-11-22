package net.CoffeDino.testmod.item;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItemProperties {

    @SubscribeEvent
    public static void registerModels(RegisterClientReloadListenersEvent event) {

        ItemProperties.register(
                ModItems.AGNIS_FURY.get(),
                ResourceLocation.fromNamespaceAndPath("testingcoffedinomod", "charged"),
                (stack, level, entity, seed) -> {

                    if (stack.has(DataComponents.CUSTOM_DATA)) {
                        var tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
                        if (tag.contains("Charged") && tag.getBoolean("Charged")) {
                            return 1.0F;
                        }
                    }

                    return 0.0F;
                }
        );
    }
}
