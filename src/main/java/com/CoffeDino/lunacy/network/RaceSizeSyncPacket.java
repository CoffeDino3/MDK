package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.capability.RaceSizeCapability;
import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RaceSizeSyncPacket(float height, float width) implements CustomPacketPayload {
    public static final Type<RaceSizeSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "race_size_sync")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, RaceSizeSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, RaceSizeSyncPacket::height,
            ByteBufCodecs.FLOAT, RaceSizeSyncPacket::width,
            RaceSizeSyncPacket::new
    );

    public static void handle(RaceSizeSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            Player player = mc.player;
            RaceSizeCapability raceSize = player.getData(ModAttachments.RACE_SIZE);
            raceSize.setRaceSize(packet.height(), packet.width());
            player.refreshDimensions();
            Lunacy.LOGGER.debug("[DEBUG] Synced size: " + packet.height() + "x" + packet.width());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}