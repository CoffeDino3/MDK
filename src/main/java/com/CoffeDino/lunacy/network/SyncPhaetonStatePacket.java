package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.item.Custom.PhaetonItem;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPhaetonStatePacket(long riseEnd, long launchEnd, boolean diving) implements CustomPacketPayload {
    public static final Type<SyncPhaetonStatePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "sync_phaeton_state")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, SyncPhaetonStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SyncPhaetonStatePacket::riseEnd,
            ByteBufCodecs.VAR_LONG, SyncPhaetonStatePacket::launchEnd,
            ByteBufCodecs.BOOL, SyncPhaetonStatePacket::diving,
            SyncPhaetonStatePacket::new
    );

    public static void handle(SyncPhaetonStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            PhaetonItem.setClientState(packet.riseEnd(), packet.launchEnd(), packet.diving());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}