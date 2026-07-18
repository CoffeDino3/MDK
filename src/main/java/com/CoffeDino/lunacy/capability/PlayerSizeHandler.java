package com.CoffeDino.lunacy.capability;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Lunacy.MODID)
public class PlayerSizeHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) return;

        RaceSizeCapability raceSize = player.getData(ModAttachments.RACE_SIZE);
        float height = raceSize.getRaceHeight();
        float width = raceSize.getRaceWidth();

        EntityDimensions dims = player.getDimensions(player.getPose());
        if (dims.height() != height || dims.width() != width) {
            player.refreshDimensions();
        }
    }

    public static float getCustomEyeHeight(float height, Pose pose) {
        float base = height * 0.9f;
        return switch (pose) {
            case SWIMMING, FALL_FLYING -> base * 0.4f;
            case CROUCHING -> base * 0.8f;
            case SLEEPING -> 0.2f;
            default -> base;
        };
    }
}