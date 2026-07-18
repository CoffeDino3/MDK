package com.CoffeDino.lunacy.client;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.client.gui.SculkStorageScreen;
import com.CoffeDino.lunacy.client.model.Shi_arrows;
import com.CoffeDino.lunacy.entity.ModEntities;
import com.CoffeDino.lunacy.item.ModItems;
import com.CoffeDino.lunacy.menu.ModMenuTypes;
import com.CoffeDino.lunacy.renderer.ShiArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Lunacy.MODID, value = Dist.CLIENT)
public class ClientRegistry {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.SHI_BOW.get(),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null) return 0.0F;
                        return entity.getUseItem() != stack ? 0.0F
                                : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
                    });

            ItemProperties.register(ModItems.SHI_BOW.get(),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "pulling"),
                    (stack, level, entity, seed) ->
                            entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            EntityRenderers.register(ModEntities.SHI_ARROW.get(), ShiArrowRenderer::new);

        });
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SCULK_STORAGE.get(), SculkStorageScreen::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(Shi_arrows.LAYER_LOCATION, Shi_arrows::createBodyLayer);
    }
}