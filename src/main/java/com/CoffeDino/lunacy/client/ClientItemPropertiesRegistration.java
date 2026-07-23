package com.CoffeDino.lunacy.client;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.item.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Lunacy.MODID, value = Dist.CLIENT)
public class ClientItemPropertiesRegistration {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.JORO.get(),
                    ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "charging"),
                    (stack, level, entity, seed) ->
                            (entity != null && entity.isUsingItem() && entity.getUseItem() == stack) ? 1.0f : 0.0f
            );
            ItemProperties.register(
                    ModItems.JORO.get(),
                    ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "armed"),
                    (stack, level, entity, seed) ->
                            JoroArmedState.isArmed(entity) ? 1.0f : 0.0f
            );
            ItemProperties.register(
                    ModItems.PERUN.get(),
                    ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "charging"),
                    (stack, level, entity, seed) ->
                            (entity != null && entity.isUsingItem() && entity.getUseItem() == stack) ? 1.0f : 0.0f
            );
        });
    }
}
