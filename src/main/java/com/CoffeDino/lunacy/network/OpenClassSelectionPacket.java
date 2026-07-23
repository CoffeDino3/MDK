package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenClassSelectionPacket() implements CustomPacketPayload {
    public static final Type<OpenClassSelectionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "open_class_selection")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, OpenClassSelectionPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenClassSelectionPacket());

    public static void handle(OpenClassSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Lunacy.LOGGER.debug("DEBUG: Open class selection packet received");
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}