package com.CoffeDino.lunacy.item;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = Lunacy.MODID, value = Dist.CLIENT)
public class ModItemProperties {

    @SubscribeEvent
    public static void registerModels(RegisterClientReloadListenersEvent event) {
        ItemProperties.register(
                ModItems.AGNIS_FURY.get(),
                ResourceLocation.fromNamespaceAndPath("lunacy", "charged"),
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