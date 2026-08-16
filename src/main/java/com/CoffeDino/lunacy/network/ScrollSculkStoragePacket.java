package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.menu.SculkStorageMenu;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public record ScrollSculkStoragePacket(int scrollRows) implements CustomPacketPayload {
    public static final Type<ScrollSculkStoragePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "scroll_sculk_storage")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, ScrollSculkStoragePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ScrollSculkStoragePacket::scrollRows,
                    ScrollSculkStoragePacket::new
            );

    public static void handle(ScrollSculkStoragePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof SculkStorageMenu menu) {
                menu.remapVisibleSlots(packet.scrollRows());
                menu.broadcastChanges();
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}