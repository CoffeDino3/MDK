package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.abilities.EtherealAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record UpdateEtherealInputPacket(boolean jumping, boolean shifting) implements CustomPacketPayload {
    public static final Type<UpdateEtherealInputPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "update_ethereal_input")
    );

    public UpdateEtherealInputPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(jumping);
        buf.writeBoolean(shifting);
    }

    public static void handle(UpdateEtherealInputPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                EtherealAbilityHandler.updateEtherealInput(player, packet.jumping(), packet.shifting());
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
