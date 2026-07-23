package com.CoffeDino.lunacy.client;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.item.ModItems; // wherever your JoroItem is registered
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Lunacy.MODID, value = Dist.CLIENT)
public class ClientExtensionsRegistration {

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(JoroClientExtensions.INSTANCE, ModItems.JORO.get());
    }
}