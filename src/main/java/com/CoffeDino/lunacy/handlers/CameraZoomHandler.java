package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.item.Custom.GunItem;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Lunacy.MODID, value = Dist.CLIENT)
public class CameraZoomHandler {
    private static final float MAX_ZOOM = 0.2f;

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
            ItemStack mainHand = mc.player.getMainHandItem();
            if (mainHand.getItem() instanceof GunItem && mc.player.isUsingItem() && mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                float progress = GunUsageHandler.getAimProgress(mc.player);
                float zoomLevel = progress * MAX_ZOOM;

                event.setFOV(event.getFOV() * (1.0f - zoomLevel));
            }
        }
    }
}