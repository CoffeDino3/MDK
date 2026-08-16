package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.WarderAbilityHandler;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AdjustWarderRingSizePacket(boolean scrollUp) implements CustomPacketPayload {
    public static final Type<AdjustWarderRingSizePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "adjust_warder_ring_size")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, AdjustWarderRingSizePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, AdjustWarderRingSizePacket::scrollUp,
                    AdjustWarderRingSizePacket::new
            );

    public static void handle(AdjustWarderRingSizePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                WarderAbilityHandler.adjustRingSize(player, packet.scrollUp());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
