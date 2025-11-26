package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record OpenClassSelectionPacket() implements CustomPacketPayload {
    public static final Type<OpenClassSelectionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "open_class_selection")
    );

    public OpenClassSelectionPacket(FriendlyByteBuf buf) {
        this();
    }

    public void encode(FriendlyByteBuf buf) {
        // No data to write
    }

    public static void handle(OpenClassSelectionPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            // This packet just triggers the screen opening on client
            // The actual screen opening is handled in the keybind
            System.out.println("DEBUG: Open class selection packet received");
        });
        context.setPacketHandled(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}