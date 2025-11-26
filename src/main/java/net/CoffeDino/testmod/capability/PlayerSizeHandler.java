package net.CoffeDino.testmod.capability;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Lunacy.MOD_ID)
public class PlayerSizeHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;

        if (player.level().isClientSide()) return;

        player.getCapability(RaceSizeProvider.RACE_SIZE).ifPresent(raceSize -> {
            float height = raceSize.getRaceHeight();
            float width = raceSize.getRaceWidth();

            EntityDimensions dims = player.getDimensions(player.getPose());
            if (dims.height() != height || dims.width() != width) {
                player.refreshDimensions();
                System.out.println("DEBUG: Refreshed size for " + player.getName().getString()
                        + " to H=" + height + " W=" + width);
            }
        });
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