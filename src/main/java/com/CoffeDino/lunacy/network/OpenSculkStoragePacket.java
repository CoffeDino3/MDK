package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.menu.SculkStorageMenuProvider;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenSculkStoragePacket() implements CustomPacketPayload {
    public static final Type<OpenSculkStoragePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "open_sculk_storage")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, OpenSculkStoragePacket> STREAM_CODEC =
            StreamCodec.unit(new OpenSculkStoragePacket());

    public static void handle(OpenSculkStoragePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (!player.getData(ModAttachments.SCULK_STORAGE).isOnCooldown()) {
                    player.openMenu(new SculkStorageMenuProvider(player));
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}