package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.EtherealAbilityHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateEtherealInputPacket(boolean jumping, boolean shifting) implements CustomPacketPayload {
    public static final Type<UpdateEtherealInputPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "update_ethereal_input")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateEtherealInputPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    UpdateEtherealInputPacket::jumping,
                    ByteBufCodecs.BOOL,
                    UpdateEtherealInputPacket::shifting,
                    UpdateEtherealInputPacket::new
            );

    public static void handle(UpdateEtherealInputPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                EtherealAbilityHandler.updateEtherealInput(player, packet.jumping(), packet.shifting());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}