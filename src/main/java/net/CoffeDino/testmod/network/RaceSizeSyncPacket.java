package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.capability.RaceSizeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class RaceSizeSyncPacket {
    private final float height;
    private final float width;

    public RaceSizeSyncPacket(float height, float width) {
        this.height = height;
        this.width = width;
    }

    public RaceSizeSyncPacket(FriendlyByteBuf buf) {
        this.height = buf.readFloat();
        this.width = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(height);
        buf.writeFloat(width);
    }

    public static RaceSizeSyncPacket decode(FriendlyByteBuf buf) {
        return new RaceSizeSyncPacket(buf);
    }

    public void handle() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Player player = mc.player;
        player.getCapability(RaceSizeProvider.RACE_SIZE).ifPresent(raceSize -> {
            raceSize.setRaceSize(height, width);
            player.refreshDimensions();
            System.out.println("[DEBUG] Synced size: " + height + "x" + width);
        });
    }
}
