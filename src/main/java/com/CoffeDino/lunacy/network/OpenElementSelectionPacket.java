package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.client.gui.ElementSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenElementSelectionPacket() implements CustomPacketPayload {
    public static final Type<OpenElementSelectionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "open_element_selection")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, OpenElementSelectionPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenElementSelectionPacket());

    public static void handle(OpenElementSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Lunacy.LOGGER.debug("DEBUG: Open element selection packet received");
            Minecraft.getInstance().setScreen(new ElementSelectionScreen());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}