package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.EnderTeleportHandler;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EnderTeleportPacket() implements CustomPacketPayload {
    public static final Type<EnderTeleportPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "ender_teleport")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, EnderTeleportPacket> STREAM_CODEC =
            StreamCodec.unit(new EnderTeleportPacket());

    public static void handle(EnderTeleportPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                EnderTeleportHandler.teleportPlayer(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}